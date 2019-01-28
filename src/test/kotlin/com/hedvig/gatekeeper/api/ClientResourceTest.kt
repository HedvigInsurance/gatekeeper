package com.hedvig.gatekeeper.api

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hedvig.gatekeeper.api.dto.ClientDto
import com.hedvig.gatekeeper.client.ClientManager
import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.GrantType
import com.hedvig.gatekeeper.client.persistence.ClientEntity
import com.hedvig.gatekeeper.db.JdbiConnector
import com.hedvig.gatekeeper.security.MockSecurityConfigurer
import io.dropwizard.jackson.Jackson
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.*
import javax.ws.rs.client.Entity

@ExtendWith(DropwizardExtensionsSupport::class)
internal class ClientResourceTest {
    private val jdbi = JdbiConnector.createForTest()
    private val clientManager = jdbi.onDemand(ClientManager::class.java)
    private val client1 = ClientEntity(
        clientId = UUID.randomUUID(),
        clientSecret = "very secret",
        redirectUris = setOf("https://redirect-1", "https://redirect-2"),
        authorizedGrantTypes = setOf(GrantType.AUTHORIZATION_CODE, GrantType.PASSWORD),
        clientScopes = setOf(ClientScope.MANAGE_MEMBERS, ClientScope.MANAGE_EMPLOYEES),
        createdAt = Instant.now(),
        createdBy = "Blargh"
    )
    private val client2 = ClientEntity(
        clientId = UUID.randomUUID(),
        clientSecret = "very secret",
        redirectUris = setOf("https://redirect-1", "https://redirect-2"),
        authorizedGrantTypes = setOf(GrantType.AUTHORIZATION_CODE, GrantType.PASSWORD),
        clientScopes = setOf(ClientScope.MANAGE_MEMBERS, ClientScope.MANAGE_EMPLOYEES),
        createdAt = Instant.now(),
        createdBy = "Blargh 2"
    )
    val resources = MockSecurityConfigurer(setOf("ADMIN_SYSTEM"), setOf("ADMIN_SYSTEM"))
        .configureMockSecurity(ResourceExtension.builder())
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

        val result = resources.target("/admin/clients")
            .request()
            .header("Authorization", "Bearer eyJ...")
            .get()
        assertThat(result.status).isEqualTo(200)
        assertThat(result.readEntity(Array<ClientDto>::class.java))
            .isEqualTo(
                arrayOf(ClientDto.fromClientEntity(client2), ClientDto.fromClientEntity(client1))
            )
    }

    @Test
    fun testGetsClient() {
        clientManager.insert(client1)

        val result = resources.target("/admin/clients/${client1.clientId}")
            .request()
            .header("Authorization", "Bearer eyJ...")
            .get()
        assertThat(result.status).isEqualTo(200)
        assertThat(result.readEntity(ClientDto::class.java))
            .isEqualTo(ClientDto.fromClientEntity(client1))
    }

    @Test
    fun testCreatesAClient() {
        val client = CreateClientRequestDto(
            clientScopes = setOf(ClientScope.ADMIN_SYSTEM),
            redirectUris = setOf("https://redirect-1"),
            authorizedGrantTypes = setOf(GrantType.PASSWORD)
        )
        val result = resources.target("/admin/clients")
            .request()
            .header("Authorization", "Bearer eyJ...")
            .post(Entity.json(client))
        assertThat(result.status).isEqualTo(201)

        val body = result.readEntity(ClientDto::class.java)
        assertThat(body.clientScopes).isEqualTo(client.clientScopes)
        assertThat(body.redirectUris).isEqualTo(client.redirectUris)
        assertThat(body.authorizedGrantTypes).isEqualTo(client.authorizedGrantTypes)
        assertThat(body.createdBy).isEqualTo("foo@bar.baz")
    }
}
