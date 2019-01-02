package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.client.ClientScope
import org.apache.log4j.LogManager.getLogger
import org.jdbi.v3.sqlobject.CreateSqlObject
import org.jdbi.v3.sqlobject.transaction.Transaction
import java.time.Instant
import java.util.*

interface RefreshTokenManager {
    companion object {
        private val LOG = getLogger(RefreshTokenManager::class.java)
    }

    @CreateSqlObject
    fun refreshTokenDao(): RefreshTokenDao

    @Transaction
    fun findUsableRefreshTokenByToken(token: String): Optional<RefreshTokenEntity> {
        LOG.info("Finding usable refresh token")
        return refreshTokenDao().findUsableRefreshTokenByToken(token)
    }

    @Transaction
    fun markAsUsed(token: String): Optional<RefreshTokenEntity> {
        LOG.info("Trying to mark refresh token as used")
        val refreshTokenEntity = findUsableRefreshTokenByToken(token)
        if (!refreshTokenEntity.isPresent) {
            return Optional.empty()
        }
        refreshTokenDao().markAsUsed(refreshTokenEntity.get().id)
        LOG.info("Successfully marked refresh token as used [id=${refreshTokenEntity.get().id}]")
        return refreshTokenDao().find(refreshTokenEntity.get().id)
    }

    @Transaction
    fun createRefreshToken(
        subject: String,
        clientId: UUID,
        scopes: Set<ClientScope>,
        token: String
    ): RefreshTokenEntity {
        LOG.info("Creating refresh token for subject \"$subject\"")
        val refreshToken = RefreshTokenEntity(
            id = UUID.randomUUID(),
            token = token,
            subject = subject,
            scopes = scopes,
            clientId = clientId,
            createdAt = Instant.now(),
            usedAt = null,
            revokedAt = null
        )
        refreshTokenDao().insertRefreshToken(refreshToken)
        LOG.info("Successfully created refresh token [id=${refreshToken.id}]")

        return refreshToken
    }
}
