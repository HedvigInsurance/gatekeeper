package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.db.JdbiConnector
import org.junit.Assert.*
import org.junit.jupiter.api.Test
import java.util.*

internal class RefreshTokenManagerTest {
    @Test
    fun testCreatesRefreshTokens() {
        val jdbi = JdbiConnector.createForTest()
        jdbi.useHandle<RuntimeException> { it.execute("TRUNCATE refresh_tokens;") }
        val refreshTokenDao = jdbi.onDemand(RefreshTokenDao::class.java)

        val result = refreshTokenDao.createRefreshToken(
            "blargh@blargh.com",
            UUID.randomUUID(),
            setOf(ClientScope.MANAGE_MEMBERS),
            "abc123"
        )
        assertEquals("blargh@blargh.com", result.subject)
        assertEquals(setOf(ClientScope.MANAGE_MEMBERS), result.scopes)

        assertEquals(result, refreshTokenDao.findUsableRefreshTokenByToken("abc123").get())
    }

    @Test
    fun testMarksRefreshTokensAsUsed() {
        val jdbi = JdbiConnector.createForTest()
        jdbi.useHandle<RuntimeException> { it.execute("TRUNCATE refresh_tokens;") }
        val refreshTokenDao = jdbi.onDemand(RefreshTokenDao::class.java)

        refreshTokenDao.createRefreshToken(
            "blarg@blargh.com",
            UUID.randomUUID(),
            setOf(ClientScope.MANAGE_MEMBERS),
            "abc123"
        )
        val usedRefreshToken = refreshTokenDao.markAsUsed("abc123")
        assertNotNull(usedRefreshToken.get().usedAt)
        val result = refreshTokenDao.findUsableRefreshTokenByToken("abc123")
        assertFalse(result.isPresent)
    }
}