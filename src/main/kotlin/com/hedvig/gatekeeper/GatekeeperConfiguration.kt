package com.hedvig.gatekeeper

import com.fasterxml.jackson.annotation.JsonProperty
import com.hedvig.gatekeeper.config.AuthConfiguration
import io.dropwizard.Configuration
import javax.validation.Valid

class GatekeeperConfiguration : Configuration() {
    @JsonProperty("auth")
    @Valid
    lateinit var auth: AuthConfiguration
}
