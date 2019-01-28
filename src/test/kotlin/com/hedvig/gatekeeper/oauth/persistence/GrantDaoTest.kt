package com.hedvig.gatekeeper.oauth.persistence

import com.hedvig.gatekeeper.db.JdbiConnector
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class GrantDaoTest {
    @Test
    fun createsAndFindsGrants() {
        val jdbi = JdbiConnector.createForTest()
        val dao = jdbi.onDemand(GrantDao::class.java)
        jdbi.useHandle<RuntimeException> {
            it.execute("TRUNCATE grants;")
        }

        val grant = Grant(
            id = UUID.randomUUID(),
            subject = "g:blargh@hedvig.com",
            clientId = UUID.randomUUID(),
            grantMethod = "google_sso",
            scopes = setOf("FOO"),
            grantedAt = Instant.now()
        )
        dao.insert(grant)

        val result = dao.find(grant.id).get()
        assertEquals(grant, result)
    }
}