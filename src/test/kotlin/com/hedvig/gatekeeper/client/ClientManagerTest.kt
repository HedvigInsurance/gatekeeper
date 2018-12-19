package com.hedvig.gatekeeper.client

import com.hedvig.gatekeeper.client.persistence.ClientEntity
import com.hedvig.gatekeeper.db.JdbiConnector
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class ClientManagerTest {
    @Test
    fun testInsertsAndFindsClient() {
        val jdbi = JdbiConnector.createForTest()
        val manager = jdbi.onDemand(ClientManager::class.java)
        jdbi.useHandle<RuntimeException> { it.execute("TRUNCATE clients;") }

        val client = ClientEntity(
            clientId = UUID.randomUUID(),
            clientSecret = "very secret",
            redirectUris = setOf("https://redirect-1", "https://redirect-2"),
            authorizedGrantTypes = setOf(GrantType.AUTHORIZATION_CODE, GrantType.PASSWORD),
            clientScopes = setOf(ClientScope.ROOT, ClientScope.IEX),
            createdAt = Instant.now(),
            createdBy = "Blargh"
        )
        manager.insert(client)

        val result = manager.find(client.clientId)
        assertTrue(result.isPresent)
    }
}
