package com.hedvig.gatekeeper.auth.persistence

import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.time.Instant
import java.util.*

interface RefreshTokenDao {
    @SqlUpdate("INSERT INTO refresh_tokens (id, subject, roles, token, created_at) VALUES (:id, :subject, :roles, :token, :createdAt)")
    fun create(@BindBean refreshToken: RefreshToken)

    @SqlUpdate("UPDATE refresh_tokens SET used_at = :time WHERE id = :id")
    fun markAsUsed(@Bind("id") id: UUID, @Bind("time") time: Instant = Instant.now())

    @SqlQuery("SELECT * FROM refresh_tokens WHERE id = :id")
    @RegisterRowMapper(RefreshTokenRowMapper::class)
    fun findById(@Bind("id") id: UUID): RefreshToken?

    @SqlQuery("SELECT * FROM refresh_tokens WHERE token = :token AND used_at IS NULL")
    @RegisterRowMapper(RefreshTokenRowMapper::class)
    fun findUnusedByToken(@Bind("token") token: String): RefreshToken?
}
