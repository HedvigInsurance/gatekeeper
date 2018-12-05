package com.hedvig.gatekeeper.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.hedvig.gatekeeper.auth.Role
import javax.validation.constraints.NotNull

data class IssueAdminAccessTokenResponse(
    @get:JsonProperty("access_token")
    @NotNull
    val accessToken: String,

    @get:JsonProperty("roles")
    @NotNull
    val roles: Array<Role>,

    @get:JsonProperty("expires_in")
    @NotNull
    val expiresIn: Int = 60 * 30,

    @get:JsonProperty("token_type")
    @NotNull
    val tokenType: String  = "bearer"
)