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
import com.hedvig.gatekeeper.authorization.employees.EmployeeManager
import com.hedvig.gatekeeper.client.ClientManager
import com.hedvig.gatekeeper.client.PostgresClientService
import com.hedvig.gatekeeper.db.JdbiConnector
import com.hedvig.gatekeeper.health.ApplicationHealthCheck
import com.hedvig.gatekeeper.identity.ChainedIdentityService
import com.hedvig.gatekeeper.identity.EmployeeIdentityService
import com.hedvig.gatekeeper.identity.InMemoryIdentityService
import com.hedvig.gatekeeper.oauth.GOOGLE_SSO
import com.hedvig.gatekeeper.oauth.GoogleSsoGrantAuthorizer
import com.hedvig.gatekeeper.oauth.GoogleSsoVerifier
import com.hedvig.gatekeeper.oauth.persistence.GrantPersistenceManager
import com.hedvig.gatekeeper.security.IntraServiceAuthenticator
import com.hedvig.gatekeeper.security.IntraServiceAuthorizer
import com.hedvig.gatekeeper.security.User
import com.hedvig.gatekeeper.token.JWTAccessTokenConverter
import com.hedvig.gatekeeper.token.PostgresTokenStore
import com.hedvig.gatekeeper.token.RefreshTokenManager
import com.hedvig.gatekeeper.token.SecureRandomRefreshTokenConverter
import com.hedvig.gatekeeper.utils.DotenvFacade
import com.hedvig.gatekeeper.utils.RandomGenerator
import com.hedvig.gatekeeper.web.SsoWebResource
import io.dropwizard.Application
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
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

        val substitutor = EnvironmentVariableSubstitutor(true)
        substitutor.variableResolver = DotenvEnvironmentVariableLookup(DotenvFacade.getSingleton())
        bootstrap.configurationSourceProvider = SubstitutingSourceProvider(
            bootstrap.configurationSourceProvider,
            substitutor
        )

        bootstrap.objectMapper.registerModule(KotlinModule())
    }

    override fun run(configuration: GatekeeperConfiguration, environment: Environment) {
        val jdbi = JdbiConnector.connect(configuration, environment)

        val clientManager = jdbi.onDemand(ClientManager::class.java)
        val employeeManager = jdbi.onDemand(EmployeeManager::class.java)

        val jwtAlgorithm = Algorithm.HMAC256(configuration.secrets!!.jwtSecret!!)
        val postgresTokenStore = PostgresTokenStore(jdbi.onDemand(RefreshTokenManager::class.java), jwtAlgorithm)

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

        environment.jersey().register(ClientResource(clientManager))

        environment.healthChecks().register("application", ApplicationHealthCheck())
        environment.jersey().register(HealthResource())

        val secureRandomRefreshTokenConverter = SecureRandomRefreshTokenConverter(
            RandomGenerator(SecureRandom()),
            { Instant.now() },
            configuration.refreshTokenExpirationTimeInDays!!
        )
        val oauthClientService = PostgresClientService(clientManager)
        val oauthIdentityService = ChainedIdentityService(arrayOf(
            InMemoryIdentityService("blargh", "very secure"),
            EmployeeIdentityService(employeeManager)
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
        val grantPersistenceManager = jdbi.onDemand(GrantPersistenceManager::class.java)
        val uuidCodeTokenConverter = UUIDCodeTokenConverter()
        val oauth2Server = Oauth2Server.configure {
            identityService = oauthIdentityService
            clientService = oauthClientService
            tokenStore = postgresTokenStore
            accessTokenConverter = oauthAccessTokenConverter
            refreshTokenConverter = secureRandomRefreshTokenConverter
            granters = listOf<GrantingCall.() -> Granter>(
                {
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
                            grantPersistenceManager = grantPersistenceManager,
                            callContext = callContext,
                            employeeManager = employeeManager
                        ).grantGoogleSso()
                    }
                }
            )
        }
        environment.jersey().register(oauth2Server)
        environment.jersey().register(SsoWebResource(
            selfClientId = configuration.secrets!!.selfOauth2ClientId!!,
            selfClientSecret = configuration.secrets!!.selfOauth2ClientSecret!!,
            selfHost = configuration.selfHost!!,
            googleWebClientId = configuration.secrets!!.googleWebClientId!!
        ))
    }
}
