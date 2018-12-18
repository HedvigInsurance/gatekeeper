package com.hedvig.gatekeeper.auth

import com.hedvig.gatekeeper.auth.persistence.RefreshTokenDao

class RefreshTokenSubjectProvider(
    private val refreshTokenDao: RefreshTokenDao
) : UserProvider<String> {
    override fun getSubjectFrom(credentials: String): String {
        val result = refreshTokenDao.findUnusedByToken(credentials) ?: throw SubjectNotFoundException()

        return result.subject
    }
}