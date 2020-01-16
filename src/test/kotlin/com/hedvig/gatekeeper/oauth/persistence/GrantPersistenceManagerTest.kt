package com.hedvig.gatekeeper.oauth.persistence

import com.hedvig.gatekeeper.db.JdbiConnector
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class GrantPersistenceManagerTest {
    @Test
    fun testPersistsGrant() {
        val jdbi = JdbiConnector.createForTest()
        val dao = jdbi.onDemand(GrantDao::class.java)
        jdbi.useHandle<RuntimeException> {
            it.execute("TRUNCATE grants;")
        }

        val grantToSave = Grant(
            id = UUID.randomUUID(),
            subject = "g:blargh@hedvig.com",
            clientId = UUID.randomUUID(),
            grantMethod = "google_sso",
            scopes = setOf("FOO"),
            grantedAt = Instant.now()
        )
        val storedGrant = dao.storeGrant(
            subject = grantToSave.subject,
            grantMethod = grantToSave.grantMethod,
            clientId = grantToSave.clientId,
            scopes = grantToSave.scopes
        )

        val result = dao.find(storedGrant.id).get()
        assertEquals(grantToSave.subject, result.subject)
        assertEquals(grantToSave.clientId, result.clientId)
        assertEquals(grantToSave.grantMethod, result.grantMethod)
        assertEquals(grantToSave.scopes, result.scopes)
    }
}