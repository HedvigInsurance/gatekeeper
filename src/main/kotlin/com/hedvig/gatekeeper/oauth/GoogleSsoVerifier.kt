package com.hedvig.gatekeeper.oauth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory

class GoogleSsoVerifier(
    private val clientId: String,
    private val webClientId: String,
    private val allowedHostedDomains: Set<String>
) {
    fun verifyAndFindUserFromIdToken(idToken: String): SsoUser? {
        val verifierBuilder = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance())
        verifierBuilder.audience = mutableListOf(clientId, webClientId)
        val verifier = verifierBuilder.build()

        val foundIdToken = verifier.verify(idToken)

        return foundIdToken?.let<GoogleIdToken, SsoUser?> {
            val ssoUser = SsoUser(
                email = it.payload.email,
                hostedDomain = it.payload.hostedDomain
            )

            return if (!allowedHostedDomains.contains(ssoUser.hostedDomain)) {
                null
            } else {
                ssoUser
            }
        }
    }
}

data class SsoUser(
    val email: String,
    val hostedDomain: String
)
