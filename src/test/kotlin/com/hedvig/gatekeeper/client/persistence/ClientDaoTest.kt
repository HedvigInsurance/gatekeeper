package com.hedvig.gatekeeper.client.persistence

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.GrantType
import com.hedvig.gatekeeper.db.JdbiConnector
import org.jdbi.v3.core.Jdbi
import org.junit.Assert.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class ClientDaoTest {
    @Test
    fun testInsertsAndFindsClientByIdAndSecret() {
        val jdbi = JdbiConnector.createForTest()
        val dao = jdbi.onDemand(ClientDao::class.java)
        jdbi.useHandle<RuntimeException> {
            it.execute("TRUNCATE clients;")
        }

        val client = ClientEntity(
            clientId = UUID.randomUUID(),
            clientSecret = "very secret",
            redirectUris = setOf("https://redirect-1", "https://redirect-2"),
            authorizedGrantTypes = setOf(GrantType.AUTHORIZATION_CODE, GrantType.PASSWORD),
            clientScopes = setOf(ClientScope.ROOT, ClientScope.IEX),
            createdAt = Instant.now(),
            createdBy = "Blargh"
        )
        dao.insertClient(client)

        val result = dao.find(client.clientId).get()
        assertEquals(client.clientId, result.clientId)
        assertEquals(client.redirectUris, result.redirectUris)
        assertEquals(client.authorizedGrantTypes, result.authorizedGrantTypes)
        assertEquals(client.clientScopes, result.clientScopes)
        assertEquals(client.createdBy, result.createdBy)

        val withSecretResult = dao.findClientByIdAndSecret(client.clientId, client.clientSecret).get()
        assertEquals(client.clientId, withSecretResult.clientId)
    }

    private fun insertTestData(jdbi: Jdbi, clientId: UUID) {
        val createdBy = UUID.randomUUID()
        val createdAt = "2018-12-19 07:49:00"
        jdbi.useTransaction<RuntimeException> {
            it.execute("""
                INSERT INTO clients (
                    client_id,
                    client_secret,
                    redirect_uris,
                    authorized_grant_types,
                    authorized_scopes,
                    created_at,
                    created_by
                )
                VALUES (
                    '$clientId',
                    'very secret',
                    '{"https://redirect-1", "https://redirect-2"}',
                    '{"authorization_code", "password"}',
                    '{"ROOT", "IEX"}',
                    '$createdAt',
                    '$createdBy'
                )
            """)
        }
    }
}
