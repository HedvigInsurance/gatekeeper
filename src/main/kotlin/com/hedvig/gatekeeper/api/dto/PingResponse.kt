package com.hedvig.gatekeeper.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

class PingResponse(
    @JsonProperty("response") val response: String
) {}
