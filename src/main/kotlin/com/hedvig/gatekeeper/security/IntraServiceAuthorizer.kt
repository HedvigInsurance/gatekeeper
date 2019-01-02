package com.hedvig.gatekeeper.security

import io.dropwizard.auth.Authorizer

class IntraServiceAuthorizer : Authorizer<User> {
    override fun authorize(principal: User, role: String): Boolean {
        return principal.scopes.contains(role)
    }
}