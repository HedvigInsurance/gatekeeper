package com.hedvig.gatekeeper.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.oauth.persistence.GrantDao
import com.hedvig.gatekeeper.oauth.persistence.storeGrant
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.AccessToken
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
            .withSubject("blargh")
            .withIssuer("com.hedvig.gatekeeper")
            .withAudience("abc123")
            .withArrayClaim("scopes", arrayOf("MANAGE_MEMBERS"))
            .withExpiresAt(Date.from(Instant.now().plusSeconds(1_799)))
            .sign(algorithm)
        val postgresTokenStore = PostgresTokenStore(
            refreshTokenDao = mock(RefreshTokenDao::class.java),
            grantDao = mock(GrantDao::class.java),
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
            refreshTokenDao = mock(RefreshTokenDao::class.java),
            grantDao = mock(GrantDao::class.java),
            algorithm = algorithm
        )

        val result = postgresTokenStore.accessToken(at)
        assertThat(result).isNull()
    }


    @Test
    fun testStoresRefreshTokenAndGrant() {
        val algorithm = Algorithm.HMAC256("abc123")
        val refreshTokenDao = mock(RefreshTokenDao::class.java)
        val grantPersistenceDao = mock(GrantDao::class.java)
        val postgresTokenStore = PostgresTokenStore(
            refreshTokenDao = mock(RefreshTokenDao::class.java),
            grantDao =  grantPersistenceDao,
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

        verify(grantPersistenceDao).storeGrant(
            subject = "blargh",
            clientId = UUID.fromString(clientId),
            scopes = setOf(ClientScope.MANAGE_MEMBERS.toString()),
            grantMethod = "TODO"
        )
        verify(refreshTokenDao).createRefreshToken(
            subject = "blargh",
            clientId = UUID.fromString(clientId),
            scopes = setOf(ClientScope.MANAGE_MEMBERS),
            token = "abc123"
        )
    }
}
