package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.testhelp.JdbiTestHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class RefreshTokenExtensionsTest {
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
    fun testCreatesRefreshTokens() {
        val refreshTokenDao = jdbiTestHelper.jdbi.onDemand(RefreshTokenDao::class.java)

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
        val refreshTokenDao = jdbiTestHelper.jdbi.onDemand(RefreshTokenDao::class.java)

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