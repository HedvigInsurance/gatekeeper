package com.hedvig.gatekeeper.oauth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import java.util.*

class GoogleSsoVerifier(
    private val clientId: String,
    private val webClientId: String,
    private val allowedHostedDomains: Set<String>
) {
    fun verifyAndFindUserFromIdToken(idToken: String): Optional<SsoUser> {
        val verifierBuilder = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance())
        verifierBuilder.audience = mutableListOf(clientId, webClientId)
        val verifier = verifierBuilder.build()

        val foundIdToken = Optional.ofNullable(verifier.verify(idToken))
        return foundIdToken
            .map {
                SsoUser(
                    email = it.payload.email,
                    hostedDomain = it.payload.hostedDomain
                )
            }
            .flatMap {
                if (!allowedHostedDomains.contains(it.hostedDomain)) {
                    Optional.empty()
                } else {
                    Optional.of(it)
                }
            }
    }
}

data class SsoUser(
    val email: String,
    val hostedDomain: String
)
