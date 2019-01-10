package com.hedvig.gatekeeper.identity

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService

class NaiveGIdentityService : IdentityService {
    override fun allowedScopes(forClient: Client, identity: Identity, scopes: Set<String>): Set<String> {
        return forClient.clientScopes
    }

    override fun identityOf(forClient: Client, username: String): Identity? {
        return when {
            username.startsWith("g:") -> Identity(username)
            else -> null
        }
    }

    override fun validCredentials(forClient: Client, identity: Identity, password: String): Boolean {
        return false
    }
}
