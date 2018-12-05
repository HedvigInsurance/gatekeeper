package com.hedvig.gatekeeper.auth

class GrantTypeUserProvider {
    private val grantTypeUserProviderMap = mapOf(
        Pair(GrantType.DANGEROUSLY_SKIP_USER_VERIFICATION, StaticSubjectProvider())
    )

    fun getUserProvider(grantType: GrantType): UserProvider<*> {
        return grantTypeUserProviderMap[grantType] as UserProvider<*>
    }
}
