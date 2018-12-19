package com.hedvig.gatekeeper.api

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hedvig.gatekeeper.api.dto.ClientDto
import com.hedvig.gatekeeper.client.ClientManager
import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.GrantType
import com.hedvig.gatekeeper.client.persistence.ClientEntity
import com.hedvig.gatekeeper.db.JdbiConnector
import io.dropwizard.jackson.Jackson
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.*

@ExtendWith(DropwizardExtensionsSupport::class)
internal class ClientResourceTest {
    private val jdbi = JdbiConnector.createForTest()
    private val clientManager = jdbi.onDemand(ClientManager::class.java)
    private val client1 = ClientEntity(
        clientId = UUID.randomUUID(),
        clientSecret = "very secret",
        redirectUris = setOf("https://redirect-1", "https://redirect-2"),
        authorizedGrantTypes = setOf(GrantType.AUTHORIZATION_CODE, GrantType.PASSWORD),
        clientScopes = setOf(ClientScope.ROOT, ClientScope.IEX),
        createdAt = Instant.now(),
        createdBy = "Blargh"
    )
    private val client2 = ClientEntity(
        clientId = UUID.randomUUID(),
        clientSecret = "very secret",
        redirectUris = setOf("https://redirect-1", "https://redirect-2"),
        authorizedGrantTypes = setOf(GrantType.AUTHORIZATION_CODE, GrantType.PASSWORD),
        clientScopes = setOf(ClientScope.ROOT, ClientScope.IEX),
        createdAt = Instant.now(),
        createdBy = "Blargh 2"
    )
    val resources = ResourceExtension.builder()
        .addResource(ClientResource(clientManager))
        .setMapper(Jackson.newObjectMapper().registerModule(KotlinModule()))
        .build()

    @BeforeEach
    fun setup() {
        jdbi.useHandle<RuntimeException> {
            it.execute("""TRUNCATE "clients";""")
        }
    }

    @Test
    fun testGetsAll() {
        clientManager.insert(client1)
        clientManager.insert(client2)

        val result = resources.target("/admin/clients").request().get()
        assertEquals(200, result.status)
        assertArrayEquals(
            arrayOf(ClientDto.fromClientEntity(client2), ClientDto.fromClientEntity(client1)),
            result.readEntity(Array<ClientDto>::class.java)
        )
    }
}
