package com.hedvig.gatekeeper

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import io.dropwizard.db.DataSourceFactory
import javax.validation.Valid
import javax.validation.constraints.NotNull

class GatekeeperConfiguration : Configuration() {
    @JsonProperty("database")
    @Valid
    @NotNull
    var dataSourceFactory = DataSourceFactory()

    @JsonProperty("refreshTokenExpirationTimeInDays")
    @Valid
    @NotNull
    var refreshTokenExpirationTimeInDays: Long? = null

    @JsonProperty("accessTokenExpirationTimeInSeconds")
    @Valid
    @NotNull
    var accessTokenExpirationTimeInSeconds: Long? = null

    @JsonProperty("allowedHostedDomains")
    @Valid
    @NotNull
    var allowedHostedDomains: Set<String>? = null
}
