package com.hedvig.gatekeeper.identity

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService

class ChainedIdentityService(private val identityServices: Array<IdentityService>) : IdentityService {
    override fun allowedScopes(forClient: Client, identity: Identity, scopes: Set<String>): Set<String> {
        return identityServices.fold(emptySet()) { allowedScopes, identityService ->
            allowedScopes + identityService.allowedScopes(forClient, identity, scopes)
        }
    }

    override fun identityOf(forClient: Client, username: String): Identity? {
        for (identityService in identityServices) {
            val identity = identityService.identityOf(forClient, username)
            if (identity != null) {
                return identity
            }
        }

        return null
    }

    override fun validCredentials(forClient: Client, identity: Identity, password: String): Boolean {
        for (identityService in identityServices) {
            if (identityService.validCredentials(forClient, identity, password)) {
                return true
            }
        }

        return false
    }
}
