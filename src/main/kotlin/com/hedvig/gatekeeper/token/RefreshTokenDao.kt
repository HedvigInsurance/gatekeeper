package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.oauth.persistence.GrantDao.Companion.LOG
import org.apache.log4j.LogManager
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.jdbi.v3.sqlobject.transaction.Transaction
import java.time.Instant
import java.util.*

interface RefreshTokenDao {
    @SqlQuery(
        """
        SELECT * FROM "refresh_tokens"
            WHERE "token" = :token
            AND "used_at" IS NULL
            AND "revoked_at" IS NULL
        ;
    """
    )
    @RegisterRowMapper(RefreshTokenRowMapper::class)
    fun findUsableRefreshTokenByToken(@Bind("token") token: String): Optional<RefreshTokenEntity>

    @SqlUpdate(
        """
        INSERT INTO "refresh_tokens" ("id", "token", "client_id", "subject", "scopes", "created_at")
        VALUES (:id, :token, :clientId, :subject, :scopes, :createdAt);
    """
    )
    fun insertRefreshToken(@BindBean refreshTokenEntity: RefreshTokenEntity)

    @SqlUpdate("""UPDATE "refresh_tokens" SET "used_at" = now() WHERE id = :id;""")
    fun markAsUsed(@Bind("id") id: UUID)

    @SqlQuery(
        """
        SELECT * FROM "refresh_tokens" WHERE "id" = :id
    """
    )
    @RegisterRowMapper(RefreshTokenRowMapper::class)
    fun find(@Bind("id") id: UUID): Optional<RefreshTokenEntity>


    companion object {
        private val LOG = LogManager.getLogger(RefreshTokenDao::class.java)
    }
}


fun RefreshTokenDao.findUsableRefreshTokenByToken(token: String): Optional<RefreshTokenEntity> {
    LOG.info("Finding usable refresh token")
    return findUsableRefreshTokenByToken(token)
}

fun RefreshTokenDao.markAsUsed(token: String): Optional<RefreshTokenEntity> {
    LOG.info("Trying to mark refresh token as used")
    val refreshTokenEntity = findUsableRefreshTokenByToken(token)
    if (!refreshTokenEntity.isPresent) {
        return Optional.empty()
    }
    markAsUsed(refreshTokenEntity.get().id)
    LOG.info("Successfully marked refresh token as used [id=${refreshTokenEntity.get().id}]")
    return find(refreshTokenEntity.get().id)
}

fun RefreshTokenDao.createRefreshToken(
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
    insertRefreshToken(refreshToken)
    LOG.info("Successfully created refresh token [id=${refreshToken.id}]")

    return refreshToken
}