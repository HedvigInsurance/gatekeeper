package com.hedvig.gatekeeper.auth

class StaticSubjectProvider : UserProvider<String> {
    override fun getSubjectFrom(credentials: String): String {
        return credentials
    }
}
