package com.hedvig.gatekeeper.oauth

import com.hedvig.gatekeeper.oauth.persistence.GrantPersistenceManager
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.grant.GrantingCall
import nl.myndocs.oauth2.grant.throwExceptionIfUnverifiedClient
import nl.myndocs.oauth2.grant.validateScopes
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.request.ClientRequest
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.scope.ScopeParser
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.Converters
import org.slf4j.LoggerFactory.getLogger
import java.util.*

const val GOOGLE_SSO = "google_sso"

class GoogleSsoGrantAuthorizer(
    private val ssoVerifier: GoogleSsoVerifier,
    override val clientService: ClientService,
    override val identityService: IdentityService,
    override val tokenStore: TokenStore,
    private val grantPersistenceManager: GrantPersistenceManager,
    override val callContext: CallContext,
    override val converters: Converters
) : GrantingCall {
    private val LOG = getLogger(GoogleSsoGrantAuthorizer::class.java)

    fun grantGoogleSso() {
        LOG.info("Trying to authorize user from google sso")

        val googleIdToken = callContext.formParameters["google_id_token"]
            ?: throw InvalidRequestException("'google_id_token' must be provided")
        val clientRequest = ClientCredentialsOnlyClientRequest(
            clientId = callContext.formParameters["client_id"],
            clientSecret = callContext.formParameters["client_secret"]
        )
        try {
            throwExceptionIfUnverifiedClient(clientRequest)
            LOG.info("Successfully verified client")
        } catch (e: Exception) {
            LOG.warn("Unverified client [clientId='${clientRequest.clientId}']")
            throw e
        }
        val client = clientService.clientOf(clientRequest.clientId!!)!!

        LOG.info("Trying to verify user from id token")
        val ssoUser = ssoVerifier.verifyAndFindUserFromIdToken(googleIdToken)

        if (ssoUser == null) {
            LOG.warn("No user found for id token")
            throw InvalidIdentityException()
        }
        LOG.info("Successfully verified user from google id token [email='${ssoUser.email}']")

        val identity = identityService.identityOf(client, "g:${ssoUser.email}")
        if (identity == null) {
            LOG.info("No identity found for user '${ssoUser.email}'")
            throw InvalidGrantException()
        }

        val requestedScopes = ScopeParser.parseScopes(callContext.formParameters["scope"])
        try {
            validateScopes(client, identity, requestedScopes)
        } catch (e: Exception) {
            LOG.info("Failed to validate scopes [requestedScopes='$requestedScopes']")
            throw e
        }

        val accessToken = converters.accessTokenConverter.convertToToken(
            username = identity.username,
            clientId = client.clientId,
            refreshToken = converters.refreshTokenConverter.convertToToken(
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

        LOG.info("Successfully authorized user [username='${identity.username}']")
    }
}

class ClientCredentialsOnlyClientRequest(
    override val clientId: String?,
    override val clientSecret: String?
) : ClientRequest