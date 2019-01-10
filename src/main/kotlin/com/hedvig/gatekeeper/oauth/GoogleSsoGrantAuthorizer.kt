package com.hedvig.gatekeeper.oauth

import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.grant.RawRequestGrantAuthorizer
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.RawRequest
import nl.myndocs.oauth2.scope.ScopeParser

class GoogleSsoGrantAuthorizer(
    private val ssoVerifier: GoogleSsoVerifier,
    private val clientService: ClientService
) : RawRequestGrantAuthorizer() {
    override fun authorize(clientRequest: RawRequest): TokenInfo {
        val accessToken = clientRequest.callContext.formParameters["google_id_token"]
            ?: throw InvalidRequestException("'google_id_token' must be provided")

        val ssoUser = ssoVerifier.verifyAndFindUserFromIdToken(accessToken)

        if (!ssoUser.isPresent) {
            throw InvalidIdentityException()
        }

        return TokenInfo(
            identity = Identity(ssoUser.get().email),
            client = clientService.clientOf(clientRequest.clientId!!)!!,
            scopes = ScopeParser.parseScopes(clientRequest.callContext.formParameters["scope"])
        )
    }
}
