package com.hedvig.gatekeeper.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenIntrospectionResponse(
    @JsonProperty("roles") val roles: Array<String>,
    @JsonProperty("subject") val subject: String,
    @JsonProperty("exp") val expires: Long
)
