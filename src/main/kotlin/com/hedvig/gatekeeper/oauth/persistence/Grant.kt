package com.hedvig.gatekeeper.oauth.persistence

import java.time.Instant
import java.util.UUID

data class Grant(
    val id: UUID,
    val subject: String,
    val grantMethod: String,
    val clientId: UUID,
    val scopes: Set<String>,
    val grantedAt: Instant,
    val revokedAt: Instant? = null
)
