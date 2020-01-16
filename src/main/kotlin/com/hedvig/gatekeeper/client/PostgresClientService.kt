package com.hedvig.gatekeeper.client

import com.hedvig.gatekeeper.client.persistence.ClientDao
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import java.util.*

class PostgresClientService(
    private val clientDao: ClientDao
) : ClientService {
    override fun clientOf(clientId: String): Client? {
        return clientDao.find(UUID.fromString(clientId)).map { it.toClient() }.orElse(null)
    }

    override fun validClient(client: Client, clientSecret: String): Boolean {
        return clientDao
            .find(UUID.fromString(client.clientId))
            .filter { it.clientSecret == clientSecret }
            .isPresent
    }
}
