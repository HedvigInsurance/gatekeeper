package com.hedvig.gatekeeper.auth.persistence

import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface RefreshTokenDao {
    @SqlUpdate("INSERT INTO refresh_tokens (id, subject, token, created_at) VALUES (:id, :subject, :token, :createdAt)")
    fun createRefreshToken(@BindBean refreshToken: RefreshToken)

    @SqlQuery("SELECT id, subject, token, created_at, used_at FROM refresh_tokens WHERE id = :id")
    @RegisterRowMapper(RefreshTokenRowMapper::class)
    fun findRefreshTokenById(@Bind("id") id: UUID): RefreshToken
}
