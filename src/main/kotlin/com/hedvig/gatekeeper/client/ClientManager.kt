package com.hedvig.gatekeeper.client

import com.hedvig.gatekeeper.client.persistence.ClientDao
import com.hedvig.gatekeeper.client.persistence.ClientEntity
import org.jdbi.v3.sqlobject.CreateSqlObject
import org.jdbi.v3.sqlobject.transaction.Transaction
import java.util.*

interface ClientManager {
    @CreateSqlObject
    fun clientDao(): ClientDao

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
