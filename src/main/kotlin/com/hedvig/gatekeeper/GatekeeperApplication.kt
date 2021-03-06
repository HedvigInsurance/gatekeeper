package com.hedvig.gatekeeper

import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hedvig.dropwizard.config.DotenvEnvironmentVariableLookup
import com.hedvig.dropwizard.errors.UnhandledConstraintViolationRequestFilter
import com.hedvig.dropwizard.errors.ValidationErrorMessageBodyWriter
import com.hedvig.dropwizard.messages.PlainTextMessageBodyWriter
import com.hedvig.dropwizard.pebble.PebbleBundle
import com.hedvig.gatekeeper.api.ClientResource
import com.hedvig.gatekeeper.api.HealthResource
import com.hedvig.gatekeeper.api.Oauth2Server
import com.hedvig.gatekeeper.authorization.employees.EmployeeRepository
import com.hedvig.gatekeeper.client.ClientRepository
import com.hedvig.gatekeeper.client.PostgresClientService
import com.hedvig.gatekeeper.client.command.CreateClientCommand
import com.hedvig.gatekeeper.db.install
import com.hedvig.gatekeeper.health.ApplicationHealthCheck
import com.hedvig.gatekeeper.identity.ChainedIdentityService
import com.hedvig.gatekeeper.identity.EmployeeIdentityService
import com.hedvig.gatekeeper.identity.InMemoryIdentityService
import com.hedvig.gatekeeper.oauth.GOOGLE_SSO
import com.hedvig.gatekeeper.oauth.GoogleSsoGrantAuthorizer
import com.hedvig.gatekeeper.oauth.GoogleSsoVerifier
import com.hedvig.gatekeeper.oauth.GrantRepository
import com.hedvig.gatekeeper.security.IntraServiceAuthenticator
import com.hedvig.gatekeeper.security.IntraServiceAuthorizer
import com.hedvig.gatekeeper.security.User
import com.hedvig.gatekeeper.token.JWTAccessTokenConverter
import com.hedvig.gatekeeper.token.PostgresTokenStore
import com.hedvig.gatekeeper.token.RefreshTokenRepository
import com.hedvig.gatekeeper.token.SecureRandomRefreshTokenConverter
import com.hedvig.gatekeeper.utils.DotenvFacade
import com.hedvig.gatekeeper.utils.RandomGenerator
import com.hedvig.gatekeeper.web.sso.Oauth2Client
import com.hedvig.gatekeeper.web.sso.Oauth2HttpClient
import com.hedvig.gatekeeper.web.sso.RedirectValidator
import com.hedvig.gatekeeper.web.sso.SsoWebResource
import feign.Feign
import feign.form.FormEncoder
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import io.dropwizard.Application
import io.dropwizard.assets.AssetsBundle
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.db.PooledDataSourceFactory
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.migrations.MigrationsBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import nl.myndocs.oauth2.grant.Granter
import nl.myndocs.oauth2.grant.GrantingCall
import nl.myndocs.oauth2.grant.granter
import nl.myndocs.oauth2.token.converter.Converters
import nl.myndocs.oauth2.token.converter.UUIDCodeTokenConverter
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature
import java.security.SecureRandom
import java.time.Instant

class GatekeeperApplication : Application<GatekeeperConfiguration>() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            GatekeeperApplication().run(*args)
        }
    }

    override fun getName(): String {
        return "GatekeeperApplication"
    }

    override fun initialize(bootstrap: Bootstrap<GatekeeperConfiguration>) {
        bootstrap.addBundle(PebbleBundle())
        bootstrap.addBundle(AssetsBundle("/assets", "/assets"))
        bootstrap.addBundle(object : MigrationsBundle<GatekeeperConfiguration>() {
            override fun getDataSourceFactory(configuration: GatekeeperConfiguration): PooledDataSourceFactory =
                configuration.dataSourceFactory
        })

        val substitutor = EnvironmentVariableSubstitutor(true)
        substitutor.setVariableResolver(DotenvEnvironmentVariableLookup(DotenvFacade.getSingleton()))
        bootstrap.configurationSourceProvider = SubstitutingSourceProvider(
            bootstrap.configurationSourceProvider,
            substitutor
        )

        bootstrap.objectMapper.registerModule(KotlinModule())

        bootstrap.addCommand(CreateClientCommand())
    }

    override fun run(configuration: GatekeeperConfiguration, environment: Environment) {
        val factory = JdbiFactory()
        val jdbi = factory.build(environment, configuration.dataSourceFactory, "postgresql")
        jdbi.install()

        val clientRepository = ClientRepository(jdbi)
        val employeeRepository = EmployeeRepository(jdbi)
        val grantRepository = GrantRepository(jdbi)

        val jwtAlgorithm = Algorithm.HMAC256(configuration.secrets!!.jwtSecret!!)
        val postgresTokenStore = PostgresTokenStore(
            refreshTokenRepository = RefreshTokenRepository(jdbi),
            grantRepository = grantRepository,
            algorithm = jwtAlgorithm
        )

        environment.jersey().register(UnhandledConstraintViolationRequestFilter())
        environment.jersey().register(ValidationErrorMessageBodyWriter::class.java)
        environment.jersey().register(PlainTextMessageBodyWriter::class.java)

        val authFilter = AuthDynamicFeature(
            OAuthCredentialAuthFilter.Builder<User>()
                .setAuthenticator(IntraServiceAuthenticator(postgresTokenStore))
                .setAuthorizer(IntraServiceAuthorizer())
                .setPrefix("Bearer")
                .buildAuthFilter()
        )
        environment.jersey().register(authFilter)
        environment.jersey().register(RolesAllowedDynamicFeature::class.java)
        environment.jersey().register(AuthValueFactoryProvider.Binder(User::class.java))
        environment.jersey().register(AuthValueFactoryProvider.Binder(User::class.java))

        environment.jersey().register(ClientResource(clientRepository))

        environment.healthChecks().register("application", ApplicationHealthCheck())
        environment.jersey().register(HealthResource())

        val secureRandomRefreshTokenConverter = SecureRandomRefreshTokenConverter(
            RandomGenerator(SecureRandom()),
            { Instant.now() },
            configuration.refreshTokenExpirationTimeInDays!!
        )
        val oauthClientService = PostgresClientService(clientRepository)
        val oauthIdentityService = ChainedIdentityService(arrayOf(
            InMemoryIdentityService("blargh", "very secure"),
            EmployeeIdentityService(employeeRepository)
        ))
        val oauthAccessTokenConverter = JWTAccessTokenConverter(
            jwtAlgorithm,
            { Instant.now() },
            configuration.accessTokenExpirationTimeInSeconds!!
        )
        val googleSsoVerifier = GoogleSsoVerifier(
            clientId = configuration.secrets!!.googleClientId!!,
            webClientId = configuration.secrets!!.googleWebClientId!!,
            allowedHostedDomains = configuration.allowedHostedDomains!!
        )
        val uuidCodeTokenConverter = UUIDCodeTokenConverter()
        val oauth2Server = Oauth2Server.configure {
            identityService = oauthIdentityService
            clientService = oauthClientService
            tokenStore = postgresTokenStore
            accessTokenConverter = oauthAccessTokenConverter
            refreshTokenConverter = secureRandomRefreshTokenConverter
            granters = listOf<GrantingCall.() -> Granter> {
                granter(GOOGLE_SSO) {
                    GoogleSsoGrantAuthorizer(
                        ssoVerifier = googleSsoVerifier,
                        clientService = oauthClientService,
                        identityService = oauthIdentityService,
                        converters = Converters(
                            accessTokenConverter = oauthAccessTokenConverter,
                            refreshTokenConverter = secureRandomRefreshTokenConverter,
                            codeTokenConverter = uuidCodeTokenConverter
                        ),
                        tokenStore = postgresTokenStore,
                        callContext = callContext,
                        employeeRepository = employeeRepository
                    ).grantGoogleSso()
                }
            }
            tokenInfoCallback = { tokenInfo ->
                mapOf(
                    "id" to tokenInfo.identity?.metadata?.get("id"),
                    "subject" to tokenInfo.identity?.username,
                    "scopes" to tokenInfo.scopes,
                    "role" to tokenInfo.identity?.metadata?.get("role")
                )
                    .filterNot { entry -> entry.value == null }
            }
        }
        environment.jersey().register(oauth2Server)

        environment.jersey().register(SsoWebResource(
            oauth2Client = Oauth2Client(
                selfClientId = configuration.secrets!!.selfOauth2ClientId!!,
                selfClientSecret = configuration.secrets!!.selfOauth2ClientSecret!!,
                client = Feign.builder()
                    .client(OkHttpClient())
                    .encoder(JacksonEncoder(environment.objectMapper))
                    .decoder(JacksonDecoder(environment.objectMapper))
                    .encoder(FormEncoder())
                    .logger(Slf4jLogger(Oauth2HttpClient::class.java))
                    .target(Oauth2HttpClient::class.java, configuration.selfHost)
            ),
            redirectValidator = RedirectValidator(configuration.allowedRedirectDomains!!),
            googleWebClientId = configuration.secrets!!.googleWebClientId!!
        ))
    }
}
