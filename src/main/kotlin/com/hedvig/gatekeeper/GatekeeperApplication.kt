package com.hedvig.gatekeeper

import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hedvig.dropwizard.errors.UnhandledConstraintViolationRequestFilter
import com.hedvig.dropwizard.errors.ValidationErrorMessageBodyWriter
import com.hedvig.dropwizard.messages.PlainTextMessageBodyWriter
import com.hedvig.dropwizard.pebble.PebbleBundle
import com.hedvig.gatekeeper.api.ClientResource
import com.hedvig.gatekeeper.api.HealthResource
import com.hedvig.gatekeeper.api.Oauth2Server
import com.hedvig.gatekeeper.client.ClientManager
import com.hedvig.gatekeeper.client.PostgresClientService
import com.hedvig.gatekeeper.db.JdbiConnector
import com.hedvig.gatekeeper.health.ApplicationHealthCheck
import com.hedvig.gatekeeper.identity.InMemoryIdentityService
import com.hedvig.gatekeeper.oauth.GoogleSsoGrantAuthorizer
import com.hedvig.gatekeeper.oauth.GoogleSsoVerifier
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
import nl.myndocs.oauth2.config.Oauth2TokenServiceBuilder
import nl.myndocs.oauth2.grant.PasswordGrantAuthorizer
import nl.myndocs.oauth2.grant.RefreshTokenGrantAuthorizer
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

        bootstrap.configurationSourceProvider = SubstitutingSourceProvider(
            bootstrap.configurationSourceProvider,
            EnvironmentVariableSubstitutor(false)
        )

        bootstrap.objectMapper.registerModule(KotlinModule())
    }

    override fun run(configuration: GatekeeperConfiguration, environment: Environment) {
        configureDataSourceFactoryWithDotenv(configuration)

        val dotenv = DotenvFacade.getSingleton()
        val jdbi = JdbiConnector.connect(configuration, environment)

        val clientManager = jdbi.onDemand(ClientManager::class.java)

        val jwtAlgorithm = Algorithm.HMAC256(dotenv.getenv("JWT_SECRET"))
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
        val oauthIdentityService = InMemoryIdentityService("blargh", "very secure")
        val oauth2Server = Oauth2Server.configure {
            tokenService = Oauth2TokenServiceBuilder.build {
                identityService = oauthIdentityService
                clientService = oauthClientService
                tokenStore = postgresTokenStore
                accessTokenConverter = JWTAccessTokenConverter(
                    jwtAlgorithm,
                    { Instant.now() },
                    configuration.accessTokenExpirationTimeInSeconds!!
                )
                refreshTokenConverter = secureRandomRefreshTokenConverter
                allowedGrantAuthorizers = mapOf(
                    "password" to PasswordGrantAuthorizer(oauthClientService, oauthIdentityService),
                    "refresh_token" to RefreshTokenGrantAuthorizer(oauthClientService, oauthIdentityService, postgresTokenStore),
                    "google_sso" to GoogleSsoGrantAuthorizer(
                        GoogleSsoVerifier(
                            dotenv.getenv("GOOGLE_CLIENT_ID")!!,
                            dotenv.getenv("GOOGLE_CLIENT_SECRET")!!,
                            dotenv.getenv("GOOGLE_WEB_CLIENT_ID")!!
                        ),
                        oauthClientService
                    )
                )
            }
        }
        environment.jersey().register(oauth2Server)
        val selfOauth2ClientId = dotenv.getenv("SELF_OAUTH2_CLIENT_ID")!!
        val selfOauth2ClientSecret = dotenv.getenv("SELF_OAUTH2_CLIENT_SECRET")!!
        environment.jersey().register(SsoWebResource(selfOauth2ClientId, selfOauth2ClientSecret))
    }

    private fun configureDataSourceFactoryWithDotenv(configuration: GatekeeperConfiguration) {
        val dsf = configuration.dataSourceFactory
        val dotenv = DotenvFacade.getSingleton()
        dsf.url =
            if (dsf.url != "dotenv") {
                dsf.url
            } else {
                dotenv.getenv("DATABASE_JDBC")
            }
        dsf.user =
            if (dsf.user != "dotenv") {
                dsf.user
            } else {
                dotenv.getenv("DATABASE_USER")
            }
        dsf.password =
            if (dsf.password != "dotenv") {
                dsf.password
            } else {
                dotenv.getenv("DATABASE_PASSWORD")
            }
    }
}
