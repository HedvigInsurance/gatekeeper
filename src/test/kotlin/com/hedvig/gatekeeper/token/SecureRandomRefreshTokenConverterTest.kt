package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.utils.RandomGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.Instant
import java.util.*

internal const val VERY_SECURE_IN_BASE64 = "dmVyeSBzZWN1cmU="

internal class SecureRandomRefreshTokenConverterTest {
    @Test
    fun testCreatesRefreshToken() {
        val randomGenerator = mock(RandomGenerator::class.java)
        val secureRandomRefreshTokenConverter = SecureRandomRefreshTokenConverter(
            randomGenerator,
            { Instant.now() },
            60
        )

        `when`(randomGenerator.getBytes(512)).thenReturn("very secure".toByteArray(Charsets.UTF_8))

        val clientId = UUID.randomUUID()
        val result = secureRandomRefreshTokenConverter.convertToToken("blargh", clientId.toString(), setOf("MANAGE_MEMBERS"))

        assertThat(result.refreshToken).isEqualTo(VERY_SECURE_IN_BASE64)
        assertThat(result.username).isEqualTo("blargh")
        assertThat(result.clientId).isEqualTo(clientId.toString())
        assertThat(result.scopes).isEqualTo(setOf("MANAGE_MEMBERS"))
    }
}