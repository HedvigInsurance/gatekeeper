package com.hedvig.gatekeeper.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.GrantType
import com.hedvig.gatekeeper.client.persistence.ClientEntity
import java.time.Instant
import java.util.*

data class ClientDto(
    @JsonProperty("client_id")
    val clientId: UUID,

    @JsonProperty("client_scopes")
    val clientScopes: Set<ClientScope>,

    @JsonProperty("redirect_uris")
    val redirectUris: Set<String>,

    @JsonProperty("authorized_grant_types")
    val authorizedGrantTypes: Set<GrantType>,

    @JsonProperty("created_by")
    val createdBy: String,

    @JsonProperty("created_at")
    val createdAt: Instant
) {
    companion object {
        fun fromClientEntity(clientEntity: ClientEntity): ClientDto {
            return ClientDto(
                clientId = clientEntity.clientId,
                clientScopes = clientEntity.clientScopes,
                authorizedGrantTypes = clientEntity.authorizedGrantTypes,
                redirectUris = clientEntity.redirectUris,
                createdAt = clientEntity.createdAt,
                createdBy = clientEntity.createdBy
            )
        }
    }
}
