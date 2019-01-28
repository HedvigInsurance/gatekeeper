package com.hedvig.gatekeeper.client.persistence

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.GrantType
import com.hedvig.gatekeeper.db.JdbiConnector
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

    @Test
    fun testInsertsAndFindsAllClients() {
        val jdbi = JdbiConnector.createForTest()
        val dao = jdbi.onDemand(ClientDao::class.java)
        jdbi.useHandle<RuntimeException> {
            it.execute("TRUNCATE clients;")
        }

        val client1 = ClientEntity(
            clientId = UUID.randomUUID(),
            clientSecret = "very secret",
            redirectUris = setOf("https://redirect-1", "https://redirect-2"),
            authorizedGrantTypes = setOf(GrantType.AUTHORIZATION_CODE, GrantType.PASSWORD),
            clientScopes = setOf(ClientScope.MANAGE_MEMBERS, ClientScope.MANAGE_EMPLOYEES),
            createdAt = Instant.now(),
            createdBy = "Blargh"
        )
        val client2 = ClientEntity(
            clientId = UUID.randomUUID(),
            clientSecret = "very secret",
            redirectUris = setOf("https://redirect-1", "https://redirect-2"),
            authorizedGrantTypes = setOf(GrantType.AUTHORIZATION_CODE, GrantType.PASSWORD),
            clientScopes = setOf(ClientScope.MANAGE_MEMBERS, ClientScope.MANAGE_EMPLOYEES),
            createdAt = Instant.now(),
            createdBy = "Blargh 2"
        )
        dao.insertClient(client1)
        dao.insertClient(client2)

        val result = dao.findAll()
        assertArrayEquals(result, arrayOf(client2, client1))
    }
}
