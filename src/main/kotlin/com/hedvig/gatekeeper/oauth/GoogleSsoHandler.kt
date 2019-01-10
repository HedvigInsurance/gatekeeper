package com.hedvig.gatekeeper.oauth

import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.oauth2.Oauth2
import java.util.*

class GoogleSsoHandler {
//    fun initializeAuthorizationAndGetRedirectUri(): String {
//        val authorizationCodeFlow = GoogleAuthorizationCodeFlow.Builder(
//            NetHttpTransport(),
//            JacksonFactory.getDefaultInstance(),
//            "client-id",
//            "client-secret",
//            setOf(
//                "https://www.googleapis.com/auth/userinfo.profile",
//                "https://www.googleapis.com/auth/userinfo.email"
//            )
//        ).build()
//
//        authorizationCodeFlow.newAuthorizationUrl()
//    }

    fun verifyAndFindUserFromAccessToken(accessToken: String): Optional<SsoUser> {
        val credential = GoogleCredential()
        credential.setFromTokenResponse(TokenResponse().setAccessToken(accessToken))
        val oauth2 = Oauth2.Builder(
            NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        ).build()
        val userInfo = oauth2.userinfo().v2().me().get().execute() ?: return Optional.empty()

        return Optional.of(
            SsoUser(
                externalId = userInfo.id!!,
                name = userInfo.name ?: "Anonymous",
                email = userInfo.email ?: "",
                hostedDomain = userInfo.hd ?: ""
            )
        )
    }
}

data class SsoUser(
    val externalId: String,
    val name: String,
    val email: String,
    val hostedDomain: String
)
