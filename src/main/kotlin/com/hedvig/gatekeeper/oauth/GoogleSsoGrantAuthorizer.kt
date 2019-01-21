package com.hedvig.gatekeeper.oauth

import com.hedvig.gatekeeper.oauth.persistence.GrantPersistenceManager
import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.request.ClientRequest
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.scope.ScopeParser
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.AccessTokenConverter
import nl.myndocs.oauth2.token.converter.RefreshTokenConverter
import java.util.*

const val GOOGLE_SSO = "google_sso"

class GoogleSsoGrantAuthorizer(
    private val ssoVerifier: GoogleSsoVerifier,
    private val clientService: ClientService,
    private val identityService: IdentityService,
    private val accessTokenConverter: AccessTokenConverter,
    private val refreshTokenConverter: RefreshTokenConverter,
    private val tokenStore: TokenStore,
    private val tokenService: TokenService,
    private val grantPersistenceManager: GrantPersistenceManager
) {
    fun grantGoogleSso(callContext: CallContext) {
        val googleIdToken = callContext.formParameters["google_id_token"]
            ?: throw InvalidRequestException("'google_id_token' must be provided")
        val clientRequest = ClientCredentialsOnlyClientRequest(
            clientId = callContext.formParameters["client_id"],
            clientSecret = callContext.formParameters["client_secret"]
        )
        tokenService.throwExceptionIfUnverifiedClient(clientRequest)
        val client = clientService.clientOf(clientRequest.clientId!!)!!

        val ssoUser = ssoVerifier.verifyAndFindUserFromIdToken(googleIdToken)

        if (!ssoUser.isPresent) {
            throw InvalidIdentityException()
        }

        val identity = identityService.identityOf(client, "g:${ssoUser.get().email}")
            ?: throw InvalidGrantException()

        val requestedScopes = ScopeParser.parseScopes(callContext.formParameters["scope"])
        tokenService.validateScopes(client, identity, requestedScopes)

        val accessToken = accessTokenConverter.convertToToken(
            username = identity.username,
            clientId = client.clientId,
            refreshToken = refreshTokenConverter.convertToToken(
                username = identity.username,
                clientId = client.clientId,
                requestedScopes = requestedScopes
            ),
            requestedScopes = requestedScopes
        )

        tokenStore.storeAccessToken(accessToken)
        grantPersistenceManager.storeGrant(
            identity.username,
            grantMethod = GOOGLE_SSO,
            clientId = UUID.fromString(client.clientId),
            scopes = requestedScopes
        )

        callContext.respondJson(
            TokenResponse(
                accessToken = accessToken.accessToken,
                refreshToken = accessToken.refreshToken!!.refreshToken,
                expiresIn = accessToken.expiresIn(),
                tokenType = accessToken.tokenType
            )
        )
    }
}

class ClientCredentialsOnlyClientRequest(
    override val clientId: String?,
    override val clientSecret: String?
) : ClientRequest
