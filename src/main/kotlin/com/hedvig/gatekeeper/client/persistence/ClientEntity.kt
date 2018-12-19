package com.hedvig.gatekeeper.client.persistence

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.GrantType
import nl.myndocs.oauth2.client.Client
import java.time.Instant
import java.util.*

data class ClientEntity(
    val clientId: UUID,
    val clientSecret: String,
    val clientScopes: Set<ClientScope>,
    val redirectUris: Set<String>,
    val authorizedGrantTypes: Set<GrantType>,
    val createdAt: Instant,
    val createdBy: String
) {
    fun toClient(): Client {
        return Client(
            clientId = clientId.toString(),
            clientScopes = clientScopes.map { it.toString() }.toSet(),
            redirectUris = redirectUris,
            authorizedGrantTypes = authorizedGrantTypes.map { it.toString() }.toSet()
        )
    }
}
