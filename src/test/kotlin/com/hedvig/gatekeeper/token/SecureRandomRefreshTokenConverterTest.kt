package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.utils.RandomGenerator
import io.mockk.every
import io.mockk.mockk
import nl.myndocs.oauth2.identity.Identity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

internal const val VERY_SECURE_IN_BASE64 = "dmVyeSBzZWN1cmU="

internal class SecureRandomRefreshTokenConverterTest {
    @Test
    fun testCreatesRefreshToken() {
        val randomGenerator = mockk<RandomGenerator>()
        val secureRandomRefreshTokenConverter = SecureRandomRefreshTokenConverter(
            randomGenerator,
            { Instant.now() },
            60
        )

        every { randomGenerator.getBytes(512) } returns "very secure".toByteArray(Charsets.UTF_8)

        val clientId = UUID.randomUUID()
        val result = secureRandomRefreshTokenConverter.convertToToken(
            Identity("blargh"),
            clientId.toString(),
            setOf("MANAGE_MEMBERS")
        )

        assertThat(result.refreshToken).isEqualTo(VERY_SECURE_IN_BASE64)
        assertThat(result.identity?.username).isEqualTo("blargh")
        assertThat(result.clientId).isEqualTo(clientId.toString())
        assertThat(result.scopes).isEqualTo(setOf("MANAGE_MEMBERS"))
    }
}