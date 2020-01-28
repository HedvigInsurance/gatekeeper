package com.hedvig.gatekeeper.client

import com.hedvig.gatekeeper.client.persistence.ClientDao
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import java.util.*

class PostgresClientService(
    private val clientRepository: ClientRepository
) : ClientService {
    override fun clientOf(clientId: String): Client? =
        clientRepository.find(UUID.fromString(clientId))?.toClient()

    override fun validClient(client: Client, clientSecret: String): Boolean {
        return clientRepository
            .find(UUID.fromString(client.clientId))
            ?.let { it.clientSecret == clientSecret }
            ?: false
    }
}
