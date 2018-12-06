package com.hedvig.gatekeeper.utils

import io.github.cdimascio.dotenv.Dotenv

class DotenvFacade(
    val dotenv: Dotenv = Dotenv.load()
) {
    fun getenv(env: String): String? {
        return System.getenv(env) ?: dotenv[env]
    }

    companion object {
        var dotenvFacade: DotenvFacade? = null

        fun getSingleton(): DotenvFacade {
            var notNullableDotenvFacade = dotenvFacade
            if (notNullableDotenvFacade != null) {
                return notNullableDotenvFacade
            }

            notNullableDotenvFacade = DotenvFacade()
            dotenvFacade = notNullableDotenvFacade
            return notNullableDotenvFacade
        }
    }
}
