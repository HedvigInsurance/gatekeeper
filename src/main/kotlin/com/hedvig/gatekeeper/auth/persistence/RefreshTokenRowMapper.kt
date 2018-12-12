package com.hedvig.gatekeeper.auth.persistence

import com.hedvig.gatekeeper.auth.Role
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.*

class RefreshTokenRowMapper : RowMapper<RefreshToken> {
    override fun map(rs: ResultSet, ctx: StatementContext?): RefreshToken {
        val rawRoles = rs.getArray("roles").array as Array<String>
        return RefreshToken(
            id = UUID.fromString(rs.getString("id")),
            subject = rs.getString("subject"),
            roles = rawRoles.map { Role.valueOf(it) }.toTypedArray(),
            token = rs.getString("token"),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            usedAt = Optional.ofNullable(
                if (rs.getTimestamp("used_at") != null) {
                    rs.getTimestamp("used_at").toInstant()
                } else {
                    null
                }
            )
        )
    }
}
