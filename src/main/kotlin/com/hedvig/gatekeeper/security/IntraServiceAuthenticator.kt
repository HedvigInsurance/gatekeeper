package com.hedvig.gatekeeper.security

import io.dropwizard.auth.Authenticator
import nl.myndocs.oauth2.token.TokenStore
import java.util.*

class IntraServiceAuthenticator(
    private val tokenStore: TokenStore
) : Authenticator<String, User> {
    override fun authenticate(credentials: String): Optional<User> {
        val at = tokenStore.accessToken(credentials) ?: return Optional.empty()

        return Optional.of(
            User(
                name = at.identity?.username ?: "system",
                scopes = at.scopes
            )
        )
    }
}
