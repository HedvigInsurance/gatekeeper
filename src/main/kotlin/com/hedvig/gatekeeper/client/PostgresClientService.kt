package com.hedvig.gatekeeper.client

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import java.util.*

class PostgresClientService(
    private val clientManager: ClientManager
) : ClientService {
    override fun clientOf(clientId: String): Client? {
        return clientManager.find(UUID.fromString(clientId)).map { it.toClient() }.orElse(null)
    }

    override fun validClient(client: Client, clientSecret: String): Boolean {
        return clientManager
            .find(UUID.fromString(client.clientId))
            .filter { it.clientSecret == clientSecret }
            .isPresent
    }
}
