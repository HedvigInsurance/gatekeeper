package com.hedvig.gatekeeper.oauth

import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.oauth2.Oauth2
import java.util.*

class GoogleSsoVerifier(
    private val clientId: String,
    private val clientSecrets: String
) {
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
        val verifierBuilder = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance())
        verifierBuilder.audience = mutableListOf(clientId)
        val verifier = verifierBuilder.build()

        val idToken = Optional.ofNullable(verifier.verify(accessToken))
        return idToken.map {
            SsoUser(
                email = it.payload.subject,
                hostedDomain = it.payload.hostedDomain
            )
        }
//        val credential = GoogleCredential()
//        credential.setFromTokenResponse(TokenResponse().setAccessToken(accessToken))
//        val oauth2 = Oauth2.Builder(
//            NetHttpTransport(),
//            JacksonFactory.getDefaultInstance(),
//            credential
//        ).build()
//        val userInfo = oauth2.userinfo().v2().me().get().execute() ?: return Optional.empty()
    }
}

data class SsoUser(
    val email: String,
    val hostedDomain: String
)
