package com.hedvig.gatekeeper

import com.hedvig.gatekeeper.api.HealthResource
import com.hedvig.gatekeeper.health.ApplicationHealthCheck
import io.dropwizard.Application
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment

class ActualGatekeeperApplication : Application<GatekeeperConfiguration>() {
    fun main(args: Array<String>) {
        ActualGatekeeperApplication().run(*args)
    }

    override fun getName(): String {
        return "ActualGatekeeperApplication"
    }

    override fun initialize(bootstrap: Bootstrap<GatekeeperConfiguration>) {
        bootstrap.configurationSourceProvider = SubstitutingSourceProvider(
            bootstrap.configurationSourceProvider,
            EnvironmentVariableSubstitutor(false)
        )
    }

    override fun run(configuration: GatekeeperConfiguration, environment: Environment) {
        environment.healthChecks().register("application", ApplicationHealthCheck())
        environment.jersey().register(HealthResource())
    }
}
