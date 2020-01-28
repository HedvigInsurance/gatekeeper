package com.hedvig.gatekeeper.oauth.persistence

import com.hedvig.gatekeeper.oauth.GrantRepository
import com.hedvig.gatekeeper.testhelp.JdbiTestHelper
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class GrantPersistenceRepositoryTest {
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
    fun testPersistsGrant() {
        val repository = GrantRepository(jdbiTestHelper.jdbi)

        val grantToSave = Grant(
            id = UUID.randomUUID(),
            subject = "g:blargh@hedvig.com",
            clientId = UUID.randomUUID(),
            grantMethod = "google_sso",
            scopes = setOf("FOO"),
            grantedAt = Instant.now()
        )
        val storedGrant = repository.storeGrant(
            subject = grantToSave.subject,
            grantMethod = grantToSave.grantMethod,
            clientId = grantToSave.clientId,
            scopes = grantToSave.scopes
        )

        val result = repository.find(storedGrant.id)
        assertEquals(grantToSave.subject, result!!.subject)
        assertEquals(grantToSave.clientId, result.clientId)
        assertEquals(grantToSave.grantMethod, result.grantMethod)
        assertEquals(grantToSave.scopes, result.scopes)
    }
}