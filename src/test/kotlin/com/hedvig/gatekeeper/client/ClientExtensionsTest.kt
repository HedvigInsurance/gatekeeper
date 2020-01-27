package com.hedvig.gatekeeper.client

import com.hedvig.gatekeeper.api.CreateClientRequestDto
import com.hedvig.gatekeeper.client.persistence.ClientDao
import com.hedvig.gatekeeper.client.persistence.ClientEntity
import com.hedvig.gatekeeper.client.persistence.create
import com.hedvig.gatekeeper.testhelp.JdbiTestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class ClientExtensionsTest {
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
    internal fun testCreatesAClientFromARequest() {
        val dao = jdbiTestHelper.jdbi.onDemand(ClientDao::class.java)

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
        val dao = jdbiTestHelper.jdbi.onDemand(ClientDao::class.java)

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
