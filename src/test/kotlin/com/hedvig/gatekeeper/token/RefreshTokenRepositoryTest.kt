package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.testhelp.JdbiTestHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class RefreshTokenRepositoryTest {
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
        val refreshTokenRepository = RefreshTokenRepository(jdbiTestHelper.jdbi)

        val result = refreshTokenRepository.createRefreshToken(
            "blargh@blargh.com",
            UUID.randomUUID(),
            setOf(ClientScope.MANAGE_MEMBERS),
            "abc123"
        )
        assertEquals("blargh@blargh.com", result!!.subject)
        assertEquals(setOf(ClientScope.MANAGE_MEMBERS), result.scopes)

        assertEquals(result, refreshTokenRepository.findUsableRefreshTokenByToken("abc123"))
    }

    @Test
    fun testMarksRefreshTokensAsUsed() {
        val refreshTokenRepository = RefreshTokenRepository(jdbiTestHelper.jdbi)

        refreshTokenRepository.createRefreshToken(
            "blarg@blargh.com",
            UUID.randomUUID(),
            setOf(ClientScope.MANAGE_MEMBERS),
            "abc123"
        )
        val usedRefreshToken = refreshTokenRepository.markAsUsed("abc123")
        assertNotNull(usedRefreshToken?.usedAt)
        val result = refreshTokenRepository.findUsableRefreshTokenByToken("abc123")
        assertNull(result)
    }
}