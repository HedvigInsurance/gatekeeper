package com.hedvig.gatekeeper.token

import com.auth0.jwt.algorithms.Algorithm
import com.hedvig.gatekeeper.client.ClientScope
import nl.myndocs.oauth2.identity.Identity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

internal class JWTAccessTokenConverterTest {
    @Test
    fun testCreatesAccessTokenWithOutExploding() {
        val jatc = JWTAccessTokenConverter(
            algorithm = Algorithm.HMAC256("blargh"),
            getNow = { Instant.now() },
            expirationTimeInSeconds = 1_800
        )

        val result = jatc.convertToToken(
            Identity("blargh"),
            "abc123",
            setOf(ClientScope.MANAGE_MEMBERS.toString()),
            null
        )

        assertThat(result.accessToken).startsWith("eyJ")
        assertThat(result.refreshToken).isNull()
        assertThat(result.identity?.username).isEqualTo("blargh")
        assertThat(result.clientId).isEqualTo("abc123")
        assertThat(result.scopes).isEqualTo(setOf(ClientScope.MANAGE_MEMBERS.toString()))
        assertThat(result.tokenType).isEqualTo("jwt")
    }
}
