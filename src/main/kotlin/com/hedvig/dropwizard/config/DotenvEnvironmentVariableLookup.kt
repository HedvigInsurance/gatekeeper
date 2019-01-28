package com.hedvig.dropwizard.config

import com.hedvig.gatekeeper.utils.DotenvFacade
import io.dropwizard.configuration.EnvironmentVariableLookup
import io.dropwizard.configuration.UndefinedEnvironmentVariableException

class DotenvEnvironmentVariableLookup(
    private val dotenvFacade: DotenvFacade,
    private val strict: Boolean = false
) : EnvironmentVariableLookup() {
    override fun lookup(key: String?): String? {
        if (key == null) {
            throw UndefinedEnvironmentVariableException("No key provided when looking up environment variable")
        }
        val result = dotenvFacade.getenv(key)
        if (result == null && strict) {
            throw UndefinedEnvironmentVariableException(
                """The environment variable '$key' is not defined; could not substitute the expression '${"$"}{$key}'."""
            )
        }

        return result
    }
}
