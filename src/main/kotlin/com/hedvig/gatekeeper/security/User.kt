package com.hedvig.gatekeeper.security

import java.security.Principal

class User(
    private val name: String,
    val scopes: Set<String>
) : Principal {
    companion object {
        fun fromTokenInfo(tokenInfo: TokenInfo): User {
            return User(
                name = tokenInfo.username,
                scopes = tokenInfo.scopes
            )
        }
    }

    override fun getName(): String {
        return name
    }
}
