package com.hedvig.gatekeeper.auth

interface UserProvider<TCredentials> {
    @Throws(SubjectNotFoundException::class)
    fun getSubjectFrom(credentials: TCredentials): String
}
