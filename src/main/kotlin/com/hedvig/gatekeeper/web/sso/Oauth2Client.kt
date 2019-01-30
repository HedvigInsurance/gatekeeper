package com.hedvig.gatekeeper.web.sso

import com.fasterxml.jackson.annotation.JsonProperty
import nl.myndocs.oauth2.exception.InvalidGrantException
import java.net.URI
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder.newClient
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MultivaluedHashMap

class Oauth2Client(
    private val selfClientId: String,
    private val selfClientSecret: String,
    private val selfHost: String,
    private val client: Client = newClient()
) {
    fun grantGoogleSso(idToken: String): TokenResponse {
        val uri = URI.create("$selfHost/oauth2/token")
        val result = client.target(uri)
            .request()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(Entity.form(MultivaluedHashMap(mapOf(
                "grant_type" to "google_sso",
                "client_id" to selfClientId,
                "client_secret" to selfClientSecret,
                "google_id_token" to idToken
            ))))

        if (result.status != 200) {
            throw InvalidGrantException()
        }

        return result.readEntity(TokenResponse::class.java)
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
