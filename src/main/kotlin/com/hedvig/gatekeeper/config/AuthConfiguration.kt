package com.hedvig.gatekeeper.config

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class AuthConfiguration(
    @JsonProperty("grantTypes")
    @NotNull
    @Valid
    var grantTypes: Array<String>
)
