package com.hedvig.gatekeeper.token

import com.auth0.jwt.algorithms.Algorithm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

internal class JWTAccessTokenConverterTest {
    @Test
    fun testCreatesAccessTokenWithOutExploding() {
        val jatc = JWTAccessTokenConverter(
            algorithm = Algorithm.HMAC256("blargh"),
            getNow = { Instant.now() }
        )

        val result = jatc.convertToToken("blargh", "abc123", setOf("ROOT"), null)

        assertThat(result.accessToken).startsWith("eyJ")
        assertThat(result.refreshToken).isNull()
        assertThat(result.username).isEqualTo("blargh")
        assertThat(result.clientId).isEqualTo("abc123")
        assertThat(result.scopes).isEqualTo(setOf("ROOT"))
        assertThat(result.tokenType).isEqualTo("jwt")
    }
}