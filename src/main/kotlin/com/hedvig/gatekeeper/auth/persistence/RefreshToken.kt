package com.hedvig.gatekeeper.auth.persistence

import com.hedvig.gatekeeper.auth.Role
import java.time.Instant
import java.util.*

data class RefreshToken(
    val id: UUID,
    val subject: String,
    val roles: Array<Role>,
    val token: String,
    val createdAt: Instant,
    val usedAt: Optional<Instant> = Optional.empty()
)
