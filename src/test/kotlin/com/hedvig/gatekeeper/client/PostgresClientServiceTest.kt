package com.hedvig.gatekeeper.client

import com.hedvig.gatekeeper.client.persistence.ClientEntity
import org.junit.Assert.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.Instant
import java.util.*

internal class PostgresClientServiceTest {
    @Test
    internal fun testGetsClient() {
        val repository = mock(ClientRepository::class.java)
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
        `when`(repository.find(eq(clientId) ?: clientId)).thenReturn(client)

        val service = PostgresClientService(repository)
        val result = service.clientOf(clientId.toString())
        assertEquals(client.toClient(), result)
    }

    @Test
    internal fun testDoesntGetEmptyClient() {
        val repository = mock(ClientRepository::class.java)
        val clientId = UUID.randomUUID()
        `when`(repository.find(eq(clientId) ?: clientId)).thenReturn(null)

        val service = PostgresClientService(repository)
        val result = service.clientOf(clientId.toString())
        assertNull(result)
    }

    @Test
    internal fun testValidatesClient() {
        val repository = mock(ClientRepository::class.java)
        val clientId = UUID.randomUUID()
        val clientEntity = ClientEntity(
            clientId = clientId,
            clientSecret = "very secret",
            redirectUris = setOf(),
            clientScopes = setOf(),
            authorizedGrantTypes = setOf(),
            createdAt = Instant.now(),
            createdBy = "blargh"
        )
        val service = PostgresClientService(repository)

        `when`(repository.find(eq(clientId) ?: clientId))
            .thenReturn(clientEntity)

        val result = service.validClient(clientEntity.toClient(), "very secret")
        assertTrue(result)

        `when`(repository.find(eq(clientId) ?: clientId))
            .thenReturn(null)

        val result2 = service.validClient(clientEntity.toClient(), "not a valid secret")
        assertFalse(result2)
    }
}