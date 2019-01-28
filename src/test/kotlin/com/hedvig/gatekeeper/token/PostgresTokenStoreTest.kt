package com.hedvig.gatekeeper.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.hedvig.gatekeeper.client.ClientScope
import nl.myndocs.oauth2.token.RefreshToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.Instant
import java.util.*

internal class PostgresTokenStoreTest {
    @Test
    fun testDecodesValidAccessToken() {
        val algorithm = Algorithm.HMAC256("abc123")
        val at = JWT.create()
            .withIssuer("com.hedvig.gatekeeper")
            .withClaim("client_id", "abc123")
            .withArrayClaim("scopes", arrayOf("MANAGE_MEMBERS"))
            .withExpiresAt(Date.from(Instant.now().plusSeconds(1_799)))
            .sign(algorithm)
        val postgresTokenStore = PostgresTokenStore(
            refreshTokenManager = mock(RefreshTokenManager::class.java),
            algorithm = algorithm
        )

        val result = postgresTokenStore.accessToken(at)
        assertThat(result).isNotNull
    }

    @Test
    fun testDoesntDecodeExpiredAccessToken() {
        val algorithm = Algorithm.HMAC256("abc123")
        val at = JWT.create()
            .withIssuer("com.hedvig.gatekeeper")
            .withClaim("client_id", "abc123")
            .withArrayClaim("scopes", arrayOf("MANAGE_MEMBERS"))
            .withExpiresAt(Date.from(Instant.now().minusSeconds(1)))
            .sign(algorithm)
        val postgresTokenStore = PostgresTokenStore(
            refreshTokenManager = mock(RefreshTokenManager::class.java),
            algorithm = algorithm
        )

        val result = postgresTokenStore.accessToken(at)
        assertThat(result).isNull()
    }

    @Test
    fun testStoresRefreshToken() {
        val algorithm = Algorithm.HMAC256("abc123")
        val refreshTokenManager = mock(RefreshTokenManager::class.java)
        val postgresTokenStore = PostgresTokenStore(
            refreshTokenManager = refreshTokenManager,
            algorithm = algorithm
        )

        val clientId = UUID.randomUUID().toString()
        val refreshToken = RefreshToken(
            "abc123",
            Instant.now(),
            "blargh",
            clientId,
            setOf("MANAGE_MEMBERS")
        )
        postgresTokenStore.storeRefreshToken(refreshToken)

        verify(refreshTokenManager).createRefreshToken(
            subject = "blargh",
            clientId = UUID.fromString(clientId),
            scopes = setOf(ClientScope.MANAGE_MEMBERS),
            token = "abc123"
        )
    }
}
