package com.hedvig.gatekeeper.auth.persistence

import java.time.Instant
import java.util.*

data class RefreshToken(
    val id: UUID,
    val subject: String,
    val token: String,
    val createdAt: Instant,
    val usedAt: Optional<Instant> = Optional.empty()
)
