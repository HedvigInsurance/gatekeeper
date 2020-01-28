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

    @Valid
    @NotNull
    var refreshTokenExpirationTimeInDays: Long? = null

    @Valid
    @NotNull
    var accessTokenExpirationTimeInSeconds: Long? = null

    @Valid
    @NotNull
    var allowedHostedDomains: Set<String>? = null
    @Valid
    @NotNull
    var allowedRedirectDomains: Set<String>? = null
    @Valid
    @NotNull
    var secureCookies: Boolean? = null
    @Valid
    @NotNull
    var cookieDomain: String? = null
    @Valid
    @NotNull
    var selfHost: String? = null

    @Valid
    @NotNull
    var secrets: Secrets? = null

    data class Secrets(
        @Valid
        @NotNull
        var jwtSecret: String? = null,

        @Valid
        @NotNull
        var googleClientId: String? = null,
        @Valid
        @NotNull
        var googleClientSecret: String? = null,
        @Valid
        @NotNull
        var googleWebClientId: String? = null,

        @Valid
        @NotNull
        var selfOauth2ClientId: String? = null,
        @Valid
        @NotNull
        var selfOauth2ClientSecret: String? = null
    )
}
