package com.hedvig.gatekeeper

import com.hedvig.gatekeeper.api.AuthIssuerResource
import com.hedvig.gatekeeper.api.HealthResource
import com.hedvig.gatekeeper.auth.AccessTokenIssuer
import com.hedvig.gatekeeper.auth.GrantType
import com.hedvig.gatekeeper.auth.GrantTypeUserProvider
import com.hedvig.gatekeeper.db.JdbiConnector
import com.hedvig.gatekeeper.health.ApplicationHealthCheck
import io.dropwizard.Application
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.jackson.Jackson
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

        val jdbi = JdbiConnector.connect(configuration, environment)
        val grantTypeUserProvider = GrantTypeUserProvider()
        val tokenIssuer = AccessTokenIssuer()

        environment.jersey().register(HealthResource())
        environment.jersey().register(AuthIssuerResource(
            configuration.auth.grantTypes.map { GrantType.fromPublicName(it) }.toTypedArray(),
            grantTypeUserProvider,
            tokenIssuer
        ))
    }
}
