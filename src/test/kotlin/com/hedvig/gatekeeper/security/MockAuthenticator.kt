package com.hedvig.gatekeeper.security

import io.dropwizard.auth.Authenticator
import java.util.*

class MockAuthenticator(private val roles: Set<String>) : Authenticator<String, User> {
    override fun authenticate(credentials: String): Optional<User> {
        return if (credentials.startsWith("eyJ")) {
            Optional.of(User(name = "foo@bar.baz", scopes = roles))
        } else {
            Optional.empty()
        }
    }
}
