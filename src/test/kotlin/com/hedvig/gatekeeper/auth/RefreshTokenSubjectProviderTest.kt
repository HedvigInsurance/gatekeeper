package com.hedvig.gatekeeper.auth

import com.hedvig.gatekeeper.auth.persistence.RefreshToken
import com.hedvig.gatekeeper.auth.persistence.RefreshTokenDao
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.time.Instant
import java.util.*

internal class RefreshTokenSubjectProviderTest {
    @Test
    fun testFindsASubjectByRefreshToken() {
        val refreshTokenStub = RefreshToken(
            id = UUID.randomUUID(),
            roles = arrayOf(Role.ROOT),
            subject = "foo@hedvig.com",
            createdAt = Instant.now(),
            token = "abc123",
            usedAt = Optional.empty()
        )

        val refreshTokenDaoStub = mock(RefreshTokenDao::class.java)
        `when`(refreshTokenDaoStub.findUnusedByToken(eq("abc123") ?: "")).thenReturn(refreshTokenStub)

        val refreshTokenSubjectProvider = RefreshTokenSubjectProvider(refreshTokenDaoStub)
        assertEquals(refreshTokenStub.subject, refreshTokenSubjectProvider.getSubjectFrom("abc123"))

        `when`(refreshTokenDaoStub.findUnusedByToken(eq("not abc123") ?: "")).thenReturn(null)
        assertThrows<SubjectNotFoundException> {
            refreshTokenSubjectProvider.getSubjectFrom("not abc123")
        }
    }
}
