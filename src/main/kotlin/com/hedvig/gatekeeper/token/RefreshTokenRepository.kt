package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.persistence.ClientDao
import com.hedvig.gatekeeper.client.persistence.ClientEntity
import com.hedvig.gatekeeper.oauth.persistence.GrantDao
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import java.time.Instant
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

class RefreshTokenRepository(private val jdbi: Jdbi) {
    fun findUsableRefreshTokenByToken(token: String): RefreshTokenEntity? {
        LOG.info("Finding usable refresh token")
        return jdbi.withHandle<RefreshTokenEntity?, RuntimeException> { handle ->
            handle.attach<RefreshTokenDao>().findUsableRefreshTokenByToken(token)
        }
    }

    fun insertRefreshToken(refreshTokenEntity: RefreshTokenEntity) {
        jdbi.useHandle<RuntimeException> { handle ->
            handle.attach<RefreshTokenDao>().insertRefreshToken(refreshTokenEntity)
        }
    }

    fun markAsUsed(token: String): RefreshTokenEntity? {
        LOG.info("Trying to mark refresh token as used")

        return findUsableRefreshTokenByToken(token)?.let { refreshTokenEntity ->
            jdbi.useHandle<RuntimeException> { handle ->
                handle.attach<RefreshTokenDao>().markAsUsed(refreshTokenEntity.id)
            }

            LOG.info("Successfully marked refresh token as used [id=${refreshTokenEntity.id}]")
            find(refreshTokenEntity.id)
        }
    }

    fun find(id: UUID) =
        jdbi.withHandle<RefreshTokenEntity?, RuntimeException> { handle ->
            handle.attach<RefreshTokenDao>().find(id)
        }
    fun createRefreshToken(
        subject: String,
        clientId: UUID,
        scopes: Set<ClientScope>,
        token: String
    ): RefreshTokenEntity? {
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
        insertRefreshToken(refreshToken)
        LOG.info("Successfully created refresh token [id=${refreshToken.id}]")

        return refreshToken
    }

    companion object {
        private val LOG = getLogger(RefreshTokenDao::class.java)
    }
}