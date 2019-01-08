package com.hedvig.gatekeeper.security

import io.dropwizard.auth.Authorizer

class MockAuthorizer(private val allowedRoles: Set<String>) : Authorizer<User> {
    override fun authorize(principal: User, role: String): Boolean {
        return allowedRoles.contains(role)
    }
}
