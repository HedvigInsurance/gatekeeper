package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.client.ClientScope
import java.time.Instant
import java.util.*

data class RefreshTokenEntity(
    val id: UUID,
    val token: String,
    val subject: String,
    val scopes: Set<ClientScope>,
    val clientId: UUID,
    val createdAt: Instant,
    val usedAt: Instant?,
    val revokedAt: Instant?
)
