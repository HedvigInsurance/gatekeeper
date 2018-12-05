package com.hedvig.gatekeeper.auth

interface UserProvider<TCredentials> {
    fun getSubjectFrom(credentials: TCredentials): String
}
