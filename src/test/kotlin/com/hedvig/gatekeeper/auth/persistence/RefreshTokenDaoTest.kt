package com.hedvig.gatekeeper.auth.persistence

import com.hedvig.gatekeeper.auth.Role
import com.hedvig.gatekeeper.db.JdbiConnector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.lang.RuntimeException
import java.time.Instant
import java.util.*

class RefreshTokenDaoTest {
    @Test
    fun testCreatesAndFindsRefreshTokensById() {
        val jdbi = JdbiConnector.createForTest()
        val dao = jdbi.onDemand(RefreshTokenDao::class.java)
        jdbi.useHandle<RuntimeException> { it.execute("TRUNCATE \"refresh_tokens\"") }

        val refreshToken = RefreshToken(
            id = UUID.randomUUID(),
            roles = arrayOf(Role.ROOT),
            subject = "foo@hedvig.com",
            token = "abc123",
            createdAt = Instant.now()
        )
        dao.create(refreshToken)
        val result = dao.findById(refreshToken.id) ?: fail("Refresh token not found in database")

        assertEquals(refreshToken.id, result.id)
        assertEquals(refreshToken.subject, result.subject)
        assertTrue(refreshToken.roles.contentEquals(result.roles))
        assertEquals(refreshToken.createdAt, result.createdAt)
        assertEquals(refreshToken.token, result.token)
        assertEquals(refreshToken.usedAt, result.usedAt)
    }

    @Test
    fun testFindsUnusedRefreshTokensOnlyAndMarksThemUsed() {
        val jdbi = JdbiConnector.createForTest()
        val dao = jdbi.onDemand(RefreshTokenDao::class.java)
        jdbi.useHandle<RuntimeException> { it.execute("TRUNCATE \"refresh_tokens\"") }

        val refreshToken = RefreshToken(
            id = UUID.randomUUID(),
            roles = arrayOf(Role.ROOT),
            subject = "foo@hedvig.com",
            token = "abc123",
            createdAt = Instant.now()
        )
        dao.create(refreshToken)
        val result = dao.findUnusedByToken("abc123") ?: fail("Refresh token not found in database")
        assertEquals(refreshToken.id, result.id)

        dao.markAsUsed(refreshToken.id)

        val result2 = dao.findUnusedByToken("abc123")
        assertEquals(null, result2)
    }
}