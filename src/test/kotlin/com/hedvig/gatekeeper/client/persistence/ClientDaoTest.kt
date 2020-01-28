package com.hedvig.gatekeeper.client.persistence

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.GrantType
import com.hedvig.gatekeeper.db.JdbiConnector
import com.hedvig.gatekeeper.testhelp.JdbiTestHelper
import org.junit.Assert.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class ClientDaoTest {
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
    fun testInsertsAndFindsClientByIdAndSecret() {
        val dao = jdbiTestHelper.jdbi.onDemand(ClientDao::class.java)

        val client = ClientEntity(
            clientId = UUID.randomUUID(),
            clientSecret = "very secret",
            redirectUris = setOf("https://redirect-1", "https://redirect-2"),
            authorizedGrantTypes = setOf(GrantType.AUTHORIZATION_CODE, GrantType.PASSWORD),
            clientScopes = setOf(ClientScope.MANAGE_EMPLOYEES),
            createdAt = Instant.now(),
            createdBy = "Blargh"
        )
        dao.insert(client)

        val result = dao.find(client.clientId)
        assertNotNull(result)
        assertEquals(client.clientId, result!!.clientId)
        assertEquals(client.redirectUris, result.redirectUris)
        assertEquals(client.authorizedGrantTypes, result.authorizedGrantTypes)
        assertEquals(client.clientScopes, result.clientScopes)
        assertEquals(client.createdBy, result.createdBy)

        val withSecretResult = dao.findClientByIdAndSecret(client.clientId, client.clientSecret)
        assertEquals(client.clientId, withSecretResult?.clientId)
    }

    @Test
    fun testInsertsAndFindsAllClients() {
        val dao = jdbiTestHelper.jdbi.onDemand(ClientDao::class.java)

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
        dao.insert(client1)
        dao.insert(client2)

        val result = dao.findAll()
        assertEquals(result, listOf(client2, client1))
    }
}
