package com.hedvig.gatekeeper.client

import com.hedvig.gatekeeper.api.CreateClientRequestDto
import com.hedvig.gatekeeper.client.persistence.ClientDao
import com.hedvig.gatekeeper.client.persistence.ClientEntity
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import java.time.Instant
import java.util.*

class ClientRepository(private val jdbi: Jdbi) {
    fun find(clientId: UUID) =
        jdbi.withHandle<ClientEntity?, RuntimeException> { handle ->
            handle.attach<ClientDao>().find(clientId)
        }

    fun findClientByIdAndSecret(clientId: UUID, clientSecret: String) =
        jdbi.withHandle<ClientEntity?, RuntimeException> { handle ->
            handle.attach<ClientDao>().findClientByIdAndSecret(clientId, clientSecret)
        }

    fun findAll(): List<ClientEntity> =
        jdbi.withHandle<List<ClientEntity>, RuntimeException> { handle ->
            handle.attach<ClientDao>().findAll()
        }

    fun insert(client: ClientEntity) {
        jdbi.useHandle<RuntimeException> { handle ->
            handle.attach<ClientDao>().insert(client)
        }
    }

    fun create(request: CreateClientRequestDto, createdBy: String): ClientEntity {
        val clientId = UUID.randomUUID()
        val clientSecret = UUID.randomUUID().toString()
        val clientEntity = ClientEntity(
            clientId = clientId,
            clientSecret = clientSecret,
            redirectUris = request.redirectUris,
            authorizedGrantTypes = request.authorizedGrantTypes,
            clientScopes = request.clientScopes,
            createdAt = Instant.now(),
            createdBy = createdBy
        )
        insert(clientEntity)
        return clientEntity
    }
}