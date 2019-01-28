package com.hedvig.gatekeeper.oauth.persistence

import org.apache.log4j.LogManager.getLogger
import org.jdbi.v3.sqlobject.CreateSqlObject
import org.jdbi.v3.sqlobject.transaction.Transaction
import java.time.Instant
import java.util.*

interface GrantPersistenceManager {
    companion object {
        val LOG = getLogger(GrantPersistenceManager::class.java)
    }

    @CreateSqlObject
    fun grantDao(): GrantDao

    @Transaction
    fun storeGrant(subject: String, grantMethod: String, clientId: UUID, scopes: Set<String>): Grant {
        LOG.info("Storing grant [subject=$subject method=$grantMethod clientId=$clientId]")

        val grant = Grant(
            id = UUID.randomUUID(),
            subject = subject,
            grantMethod = grantMethod,
            clientId = clientId,
            scopes = scopes,
            grantedAt = Instant.now()
        )
        grantDao().insert(grant)

        return grant
    }
}
