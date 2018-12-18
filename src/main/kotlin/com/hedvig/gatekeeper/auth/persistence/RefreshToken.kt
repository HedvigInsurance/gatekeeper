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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RefreshToken

        if (id != other.id) return false
        if (subject != other.subject) return false
        if (!roles.contentEquals(other.roles)) return false
        if (token != other.token) return false
        if (createdAt != other.createdAt) return false
        if (usedAt != other.usedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + roles.contentHashCode()
        result = 31 * result + token.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + usedAt.hashCode()
        return result
    }
}
