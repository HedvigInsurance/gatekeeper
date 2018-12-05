package com.hedvig.gatekeeper.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PingResponse(
    @JsonProperty("response") val response: String
)
