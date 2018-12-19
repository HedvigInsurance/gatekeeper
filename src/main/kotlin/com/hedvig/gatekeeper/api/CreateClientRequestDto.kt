package com.hedvig.gatekeeper.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.GrantType
import io.dropwizard.validation.MaxSize
import io.dropwizard.validation.MinSize
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class CreateClientRequestDto(
    @NotNull
    @MinSize(1)
    @MaxSize(20)
    @JsonProperty("client_scopes")
    val clientScopes: Set<ClientScope>,

    @NotNull
    @MinSize(1)
    @MaxSize(20)
    @JsonProperty("redirect_uris")
    val redirectUris: Set<String>,

    @NotNull
    @MinSize(1)
    @MaxSize(20)
    @JsonProperty("authorized_grant_types")
    val authorizedGrantTypes: Set<GrantType>
)
