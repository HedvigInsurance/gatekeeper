package com.hedvig.gatekeeper.client

import com.hedvig.gatekeeper.client.persistence.ClientDao
import com.hedvig.gatekeeper.client.persistence.ClientEntity
import org.junit.Assert.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.Instant
import java.util.*

internal class PostgresClientServiceTest {
    @Test
    internal fun testGetsClient() {
        val clientDaoStub = mock(ClientDao::class.java)
        val clientId = UUID.randomUUID()
        val client = ClientEntity(
            clientId = clientId,
            clientSecret = "very secret",
            clientScopes = setOf(),
            authorizedGrantTypes = setOf(),
            redirectUris = setOf(),
            createdAt = Instant.now(),
            createdBy = "blargh"
        )
        `when`(clientDaoStub.find(eq(clientId) ?: clientId)).thenReturn(Optional.of(client))

        val service = PostgresClientService(clientDaoStub)
        val result = service.clientOf(clientId.toString())
        assertEquals(client.toClient(), result)
    }

    @Test
    internal fun testDoesntGetEmptyClient() {
        val clientDaoStub = mock(ClientDao::class.java)
        val clientId = UUID.randomUUID()
        `when`(clientDaoStub.find(eq(clientId) ?: clientId)).thenReturn(Optional.empty())

        val service = PostgresClientService(clientDaoStub)
        val result = service.clientOf(clientId.toString())
        assertNull(result)
    }

    @Test
    internal fun testValidatesClient() {
        val clientDaoStub = mock(ClientDao::class.java)
        val clientId = UUID.randomUUID()
        val clientEntity =  ClientEntity(
            clientId = clientId,
            clientSecret = "very secret",
            redirectUris = setOf(),
            clientScopes = setOf(),
            authorizedGrantTypes = setOf(),
            createdAt = Instant.now(),
            createdBy = "blargh"
        )
        val service = PostgresClientService(clientDaoStub)

        `when`(
            clientDaoStub.findClientByIdAndSecret(
                eq(clientId) ?: clientId,
                eq("very secret") ?: ""
            )
        )
            .thenReturn(Optional.of(clientEntity))

        val result = service.validClient(clientEntity.toClient(), "very secret")
        assertTrue(result)

        `when`(
            clientDaoStub.findClientByIdAndSecret(
                eq(clientId) ?: clientId,
                eq("not a valid secret") ?: ""
            )
        )
            .thenReturn(Optional.empty())

        val result2 = service.validClient(clientEntity.toClient(), "not a valid secret")
        assertFalse(result2)
    }
}