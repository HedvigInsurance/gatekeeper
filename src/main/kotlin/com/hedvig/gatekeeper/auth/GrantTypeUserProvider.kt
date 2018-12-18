package com.hedvig.gatekeeper.auth

class GrantTypeUserProvider(refreshTokenSubjectProvider: RefreshTokenSubjectProvider) {
    private val grantTypeUserProviderMap: Map<GrantType, UserProvider<*>>

    init {
        this.grantTypeUserProviderMap = mapOf(
            Pair(GrantType.DANGEROUSLY_SKIP_USER_VERIFICATION, StaticSubjectProvider()),
            Pair(GrantType.REFRESH_TOKEN, refreshTokenSubjectProvider)
        )
    }

    fun getUserProvider(grantType: GrantType): UserProvider<*> {
        return grantTypeUserProviderMap[grantType] as UserProvider<*>
    }
}
