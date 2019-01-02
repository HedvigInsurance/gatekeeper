package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.db.JdbiConnector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class RefreshTokenDaoTest {
    @Test
    fun testCreatesAndFindsUnusedRefreshTokens() {
        val jdbi = JdbiConnector.createForTest()
        jdbi.useHandle<RuntimeException> { it.execute("TRUNCATE refresh_tokens;") }
        val refreshTokenDao = jdbi.onDemand(RefreshTokenDao::class.java)

        val refreshToken = RefreshTokenEntity(
            id = UUID.randomUUID(),
            token = "abc123",
            subject = "blarg@blargh.com",
            scopes = setOf(ClientScope.ROOT),
            clientId = UUID.randomUUID(),
            createdAt = Instant.now(),
            usedAt = null,
            revokedAt = null
        )

        refreshTokenDao.insertRefreshToken(refreshToken)
        assertEquals(refreshToken, refreshTokenDao.findUsableRefreshTokenByToken(refreshToken.token).get())
        assertEquals(refreshToken, refreshTokenDao.find(refreshToken.id).get())
    }

    @Test
    fun testMarksAsUsedAndDoesntFindThemAfter() {
        val jdbi = JdbiConnector.createForTest()
        jdbi.useHandle<RuntimeException> { it.execute("TRUNCATE refresh_tokens;") }
        val refreshTokenDao = jdbi.onDemand(RefreshTokenDao::class.java)

        val refreshToken = RefreshTokenEntity(
            id = UUID.randomUUID(),
            token = "abc123",
            subject = "blarg@blargh.com",
            scopes = setOf(ClientScope.ROOT),
            clientId = UUID.randomUUID(),
            createdAt = Instant.now(),
            usedAt = null,
            revokedAt = null
        )

        refreshTokenDao.insertRefreshToken(refreshToken)
        refreshTokenDao.markAsUsed(refreshToken.id)

        assertFalse(refreshTokenDao.findUsableRefreshTokenByToken(refreshToken.token).isPresent)
    }
}