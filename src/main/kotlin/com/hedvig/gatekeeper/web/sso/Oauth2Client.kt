package com.hedvig.gatekeeper.web.sso

import com.fasterxml.jackson.annotation.JsonProperty
import com.hedvig.gatekeeper.client.GrantType
import feign.FeignException
import nl.myndocs.oauth2.exception.InvalidGrantException
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import javax.ws.rs.core.MultivaluedHashMap

class Oauth2Client(
    private val selfClientId: String,
    private val selfClientSecret: String,
    private val client: Oauth2HttpClient
) {
    private val logger: Logger = getLogger(Oauth2Client::class.java)
    fun grantGoogleSso(idToken: String): TokenResponse = try {
        client.token(
            grantType = GrantType.GOOGLE_SSO,
            clientId = selfClientId,
            clientSecret = selfClientSecret,
            googleIdToken = idToken
        )
    } catch (e: FeignException.FeignClientException) {
        logger.info("Received Feign client exception", e)
        throw InvalidGrantException()
    }

    data class TokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("refresh_token")
        val refreshToken: String,
        @JsonProperty("expires_in")
        val expiresIn: Int,
        @JsonProperty("token_type")
        val tokenType: String
    )
}
