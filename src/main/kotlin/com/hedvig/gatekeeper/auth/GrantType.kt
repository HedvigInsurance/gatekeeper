package com.hedvig.gatekeeper.auth

enum class GrantType {
    DANGEROUSLY_SKIP_USER_VERIFICATION
    ;

    companion object {
        fun fromPublicName(name: String): GrantType {
            return valueOf(name.toUpperCase())
        }
    }
}
