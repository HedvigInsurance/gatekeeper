package com.hedvig.gatekeeper.client

import com.hedvig.gatekeeper.api.CreateClientRequestDto
import com.hedvig.gatekeeper.client.persistence.ClientDao
import com.hedvig.gatekeeper.client.persistence.ClientEntity
import org.jdbi.v3.sqlobject.CreateSqlObject
import org.jdbi.v3.sqlobject.transaction.Transaction
import java.time.Instant
import java.util.*

interface ClientManager {
    @CreateSqlObject
    fun clientDao(): ClientDao

    @Transaction
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

    @Transaction
    fun insert(client: ClientEntity) {
        clientDao().insertClient(client)
    }

    fun find(clientId: UUID): Optional<ClientEntity> {
        return clientDao().find(clientId)
    }

    fun findAll(): Array<ClientEntity> {
        return clientDao().findAll()
    }
}
