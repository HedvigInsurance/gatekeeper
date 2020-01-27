package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.db.JdbiConnector
import com.hedvig.gatekeeper.testhelp.JdbiTestHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class RefreshTokenDaoTest {
    private val jdbiTestHelper = JdbiTestHelper.create()

    @BeforeEach
    fun before() {
        jdbiTestHelper.before()
    }

    @AfterEach
    fun after() {
        jdbiTestHelper.after()
    }

    @Test
    fun testCreatesAndFindsUnusedRefreshTokens() {
        val refreshTokenDao = jdbiTestHelper.jdbi.onDemand(RefreshTokenDao::class.java)

        val refreshToken = RefreshTokenEntity(
            id = UUID.randomUUID(),
            token = "abc123",
            subject = "blarg@blargh.com",
            scopes = setOf(ClientScope.MANAGE_MEMBERS),
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
        val refreshTokenDao = jdbiTestHelper.jdbi.onDemand(RefreshTokenDao::class.java)

        val refreshToken = RefreshTokenEntity(
            id = UUID.randomUUID(),
            token = "abc123",
            subject = "blarg@blargh.com",
            scopes = setOf(ClientScope.MANAGE_MEMBERS),
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