package com.hedvig.gatekeeper.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.oauth.GrantRepository
import io.mockk.mockk
import io.mockk.verify
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Date
import java.util.UUID

internal class PostgresTokenStoreTest {
    @Test
    fun testDecodesValidAccessToken() {
        val algorithm = Algorithm.HMAC256("abc123")
        val at = JWT.create()
            .withSubject("blargh")
            .withIssuer("com.hedvig.gatekeeper")
            .withAudience("abc123")
            .withArrayClaim("scopes", arrayOf("MANAGE_MEMBERS"))
            .withExpiresAt(Date.from(Instant.now().plusSeconds(1_799)))
            .sign(algorithm)
        val postgresTokenStore = PostgresTokenStore(
            refreshTokenRepository = mockk(),
            grantRepository = mockk(),
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
            .withAudience("abc123")
            .withArrayClaim("scopes", arrayOf(ClientScope.MANAGE_MEMBERS.toString()))
            .withExpiresAt(Date.from(Instant.now().minusSeconds(1)))
            .sign(algorithm)
        val postgresTokenStore = PostgresTokenStore(
            refreshTokenRepository = mockk(),
            grantRepository = mockk(),
            algorithm = algorithm
        )

        val result = postgresTokenStore.accessToken(at)
        assertThat(result).isNull()
    }


    @Test
    fun testStoresRefreshTokenAndGrant() {
        val algorithm = Algorithm.HMAC256("abc123")
        val refreshTokenRepository = mockk<RefreshTokenRepository>(relaxed = true)
        val grantPersistenceRepository = mockk<GrantRepository>(relaxed = true)
        val postgresTokenStore = PostgresTokenStore(
            refreshTokenRepository = refreshTokenRepository,
            grantRepository = grantPersistenceRepository,
            algorithm = algorithm
        )

        val clientId = UUID.randomUUID().toString()
        val accessToken = AccessToken(
            accessToken = "an AT",
            clientId = clientId,
            tokenType = "jwt",
            identity = Identity("blargh"),
            expireTime = Instant.now().plusSeconds(1_800),
            scopes = setOf(ClientScope.MANAGE_MEMBERS.toString()),
            refreshToken = RefreshToken(
                "abc123",
                Instant.now(),
                Identity("blargh"),
                clientId,
                setOf(ClientScope.MANAGE_MEMBERS.toString())
            )
        )
        postgresTokenStore.storeAccessToken(accessToken)

        verify {
            grantPersistenceRepository.storeGrant(
                subject = "blargh",
                clientId = UUID.fromString(clientId),
                scopes = setOf(ClientScope.MANAGE_MEMBERS.toString()),
                grantMethod = "TODO"
            )
        }
        verify {
            refreshTokenRepository.createRefreshToken(
                subject = "blargh",
                clientId = UUID.fromString(clientId),
                scopes = setOf(ClientScope.MANAGE_MEMBERS),
                token = "abc123"
            )
        }
    }
}
