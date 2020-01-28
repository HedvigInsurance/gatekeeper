package com.hedvig.gatekeeper.oauth.persistence

import com.hedvig.gatekeeper.oauth.GrantRepository
import com.hedvig.gatekeeper.testhelp.JdbiTestHelper
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

internal class GrantDaoTest {
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
    fun createsAndFindsGrants() {
        val repository = GrantRepository(jdbiTestHelper.jdbi)

        val grant = Grant(
            id = UUID.randomUUID(),
            subject = "g:blargh@hedvig.com",
            clientId = UUID.randomUUID(),
            grantMethod = "google_sso",
            scopes = setOf("FOO"),
            grantedAt = Instant.now()
        )
        repository.insert(grant)

        val result = repository.find(grant.id)
        assertEquals(grant, result)
    }
}