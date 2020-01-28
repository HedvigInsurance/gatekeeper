package com.hedvig.gatekeeper.oauth

import com.hedvig.gatekeeper.oauth.persistence.Grant
import com.hedvig.gatekeeper.oauth.persistence.GrantDao
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import java.time.Instant
import java.util.*

class GrantRepository(private val jdbi: Jdbi) {
    fun find(id: UUID) =
        jdbi.withHandle<Grant?, RuntimeException> { handle ->
            handle.attach<GrantDao>().find(id)
        }

    fun insert(grant: Grant) {
        jdbi.useHandle<RuntimeException> { handle ->
            handle.attach<GrantDao>().insert(grant)
        }
    }

    fun storeGrant(subject: String, grantMethod: String, clientId: UUID, scopes: Set<String>): Grant {
        GrantDao.LOG.info("Storing grant [subject=$subject method=$grantMethod clientId=$clientId]")

        val grant = Grant(
            id = UUID.randomUUID(),
            subject = subject,
            grantMethod = grantMethod,
            clientId = clientId,
            scopes = scopes,
            grantedAt = Instant.now()
        )
        insert(grant)

        return grant
    }

}