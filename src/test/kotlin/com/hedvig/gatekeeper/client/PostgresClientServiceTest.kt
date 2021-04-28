package com.hedvig.gatekeeper.client

import com.hedvig.gatekeeper.client.persistence.ClientEntity
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

internal class PostgresClientServiceTest {
    @Test
    internal fun testGetsClient() {
        val repository = mockk<ClientRepository>()
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
        every { repository.find(clientId) } returns client

        val service = PostgresClientService(repository)
        val result = service.clientOf(clientId.toString())
        assertEquals(client.toClient(), result)
    }

    @Test
    internal fun testDoesntGetEmptyClient() {
        val repository = mockk<ClientRepository>()
        val clientId = UUID.randomUUID()
        every { repository.find(clientId) } returns null

        val service = PostgresClientService(repository)
        val result = service.clientOf(clientId.toString())
        assertNull(result)
    }

    @Test
    internal fun testValidatesClient() {
        val repository = mockk<ClientRepository>()
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

        every { repository.find(clientId) } returns clientEntity

        val result = service.validClient(clientEntity.toClient(), "very secret")
        assertTrue(result)

        every { repository.find(clientId) } returns null

        val result2 = service.validClient(clientEntity.toClient(), "not a valid secret")
        assertFalse(result2)
    }
}