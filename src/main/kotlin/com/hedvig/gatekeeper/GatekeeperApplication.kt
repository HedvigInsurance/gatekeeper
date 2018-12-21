package com.hedvig.gatekeeper

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hedvig.gatekeeper.api.ClientResource
import com.hedvig.gatekeeper.api.HealthResource
import com.hedvig.gatekeeper.client.ClientManager
import com.hedvig.gatekeeper.db.JdbiConnector
import com.hedvig.gatekeeper.health.ApplicationHealthCheck
import com.hedvig.gatekeeper.utils.DotenvFacade
import io.dropwizard.Application
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment

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
        bootstrap.configurationSourceProvider = SubstitutingSourceProvider(
            bootstrap.configurationSourceProvider,
            EnvironmentVariableSubstitutor(false)
        )

        bootstrap.objectMapper.registerModule(KotlinModule())
    }

    override fun run(configuration: GatekeeperConfiguration, environment: Environment) {
        configureDataSourceFactoryWithDotenv(configuration)

        val jdbi = JdbiConnector.connect(configuration, environment)

        val clientManager = jdbi.onDemand(ClientManager::class.java)
        val clientResource = ClientResource(clientManager)

        environment.jersey().register(clientResource)

        environment.healthChecks().register("application", ApplicationHealthCheck())
        environment.jersey().register(HealthResource())
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
