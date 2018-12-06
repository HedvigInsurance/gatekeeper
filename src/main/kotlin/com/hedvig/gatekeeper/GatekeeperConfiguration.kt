package com.hedvig.gatekeeper

import com.fasterxml.jackson.annotation.JsonProperty
import com.hedvig.gatekeeper.config.AuthConfiguration
import io.dropwizard.Configuration
import io.dropwizard.db.DataSourceFactory
import javax.validation.Valid
import javax.validation.constraints.NotNull

class GatekeeperConfiguration : Configuration() {
    @JsonProperty("auth")
    @Valid
    @NotNull
    lateinit var auth: AuthConfiguration

    @JsonProperty("database")
    @Valid
    @NotNull
    var dataSourceFactory = DataSourceFactory()
}
