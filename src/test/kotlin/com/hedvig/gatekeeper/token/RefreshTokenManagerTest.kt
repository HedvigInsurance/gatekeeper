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
        val refreshTokenManager = jdbi.onDemand(RefreshTokenManager::class.java)

        val result = refreshTokenManager.createRefreshToken(
            "blargh@blargh.com",
            UUID.randomUUID(),
            setOf(ClientScope.ROOT),
            "abc123"
        )
        assertEquals("blargh@blargh.com", result.subject)
        assertEquals(setOf(ClientScope.ROOT), result.scopes)

        assertEquals(result, refreshTokenManager.findUsableRefreshTokenByToken("abc123").get())
    }

    @Test
    fun testMarksRefreshTokensAsUsed() {
        val jdbi = JdbiConnector.createForTest()
        jdbi.useHandle<RuntimeException> { it.execute("TRUNCATE refresh_tokens;") }
        val refreshTokenManager = jdbi.onDemand(RefreshTokenManager::class.java)

        refreshTokenManager.createRefreshToken(
            "blarg@blargh.com",
            UUID.randomUUID(),
            setOf(ClientScope.ROOT),
            "abc123"
        )
        val usedRefreshToken = refreshTokenManager.markAsUsed("abc123")
        assertNotNull(usedRefreshToken.get().usedAt)
        val result = refreshTokenManager.findUsableRefreshTokenByToken("abc123")
        assertFalse(result.isPresent)
    }
}