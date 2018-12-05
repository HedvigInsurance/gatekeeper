package com.hedvig.gatekeeper.auth

data class AccessTokenContext(
    val subject: String,
    val audience: Array<String>,
    val roles: Array<Role>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) {
            return false
        }

        other as AccessTokenContext

        if (subject != other.subject) {
            return false
        }
        if (!audience.contentEquals(other.audience)) {
            return false
        }
        if (!roles.contentEquals(other.roles)) {
            return false
        }

        return true
    }
}
