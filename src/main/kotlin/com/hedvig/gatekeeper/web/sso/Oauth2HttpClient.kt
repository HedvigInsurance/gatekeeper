package com.hedvig.gatekeeper.web.sso

import com.hedvig.gatekeeper.client.GrantType
import feign.Headers
import feign.Param
import feign.RequestLine
import java.util.UUID
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

interface Oauth2HttpClient {
    @RequestLine("POST /oauth2/token")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/x-www-form-urlencoded"
    )
    fun token(
        @Param("grant_type")
        grantType: GrantType,
        @Param("client_id")
        clientId: String,
        @Param("client_secret")
        clientSecret: String,
        @Param("google_id_token")
        googleIdToken: String
    ): Oauth2Client.TokenResponse
}
