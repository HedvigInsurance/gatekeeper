package com.hedvig.gatekeeper.token

import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
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
    fun findUsableRefreshTokenByToken(@Bind("token") token: String): RefreshTokenEntity?

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
    fun find(@Bind("id") id: UUID): RefreshTokenEntity?
}
