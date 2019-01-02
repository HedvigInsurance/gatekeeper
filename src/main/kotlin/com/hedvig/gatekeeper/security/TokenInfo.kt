package com.hedvig.gatekeeper.security

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class TokenInfo @JsonCreator constructor(
    @JsonProperty("username")
    val username: String,
    @JsonProperty("scopes")
    val scopes: Set<String>
)
