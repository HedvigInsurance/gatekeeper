package com.hedvig.gatekeeper.client

import com.hedvig.gatekeeper.api.CreateClientRequestDto
import com.hedvig.gatekeeper.client.persistence.ClientDao
import com.hedvig.gatekeeper.client.persistence.ClientEntity
import com.hedvig.gatekeeper.client.persistence.create
import com.hedvig.gatekeeper.db.JdbiConnector
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class ClientManagerTest {
    @Test
    internal fun testCreatesAClientFromARequest() {
        val jdbi = JdbiConnector.createForTest()
        val dao = jdbi.onDemand(ClientDao::class.java)
        jdbi.useHandle<RuntimeException> { it.execute("TRUNCATE clients;") }

        val request = CreateClientRequestDto(
            clientScopes = setOf(ClientScope.MANAGE_EMPLOYEES),
            authorizedGrantTypes = setOf(GrantType.PASSWORD),
            redirectUris = setOf("https://redirect-1")
        )
        val createdBy = "john doe"
        val result = dao.create(request, createdBy)

        assertThat(result.clientScopes).isEqualTo(setOf(ClientScope.MANAGE_EMPLOYEES))
        assertThat(result.authorizedGrantTypes).isEqualTo(setOf(GrantType.PASSWORD))
        assertThat(result.redirectUris).isEqualTo(setOf("https://redirect-1"))

        assertThat(result.createdBy).isEqualTo(createdBy)
    }

    @Test
    fun testInsertsAndFindsClient() {
        val jdbi = JdbiConnector.createForTest()
        val dao = jdbi.onDemand(ClientDao::class.java)
        jdbi.useHandle<RuntimeException> { it.execute("TRUNCATE clients;") }

        val client = ClientEntity(
            clientId = UUID.randomUUID(),
            clientSecret = "very secret",
            redirectUris = setOf("https://redirect-1", "https://redirect-2"),
            authorizedGrantTypes = setOf(GrantType.AUTHORIZATION_CODE, GrantType.PASSWORD),
            clientScopes = setOf(ClientScope.MANAGE_MEMBERS, ClientScope.MANAGE_EMPLOYEES),
            createdAt = Instant.now(),
            createdBy = "Blargh"
        )
        dao.insert(client)

        val result = dao.find(client.clientId)
        assertTrue(result.isPresent)
    }
}
