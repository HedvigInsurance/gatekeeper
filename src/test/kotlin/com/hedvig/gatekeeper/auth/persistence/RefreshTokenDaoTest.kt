package com.hedvig.gatekeeper.auth.persistence

import com.hedvig.gatekeeper.db.JdbiConnector
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.lang.RuntimeException
import java.time.Instant
import java.util.*

class RefreshTokenDaoTest {
    @Test
    fun testCreatesAndFindsRefreshTokens() {
        val jdbi = JdbiConnector.createForTest()
        val dao = jdbi.onDemand(RefreshTokenDao::class.java)
        jdbi.useHandle<RuntimeException> { it.execute("TRUNCATE \"refresh_tokens\"") }

        val refreshToken = RefreshToken(
            id = UUID.randomUUID(),
            subject = "foo@hedvig.com",
            token = "abc123",
            createdAt = Instant.now()
        )
        dao.createRefreshToken(refreshToken)
        val result = dao.findRefreshTokenById(refreshToken.id)

        assertEquals(refreshToken.id, result.id)
        assertEquals(refreshToken.subject, result.subject)
        assertEquals(refreshToken.createdAt, result.createdAt)
        assertEquals(refreshToken.token, result.token)
        assertEquals(refreshToken.usedAt, result.usedAt)
    }
}