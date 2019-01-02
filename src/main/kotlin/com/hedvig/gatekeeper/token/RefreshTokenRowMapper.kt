package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.client.ClientScope
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.*

class RefreshTokenRowMapper : RowMapper<RefreshTokenEntity> {
    override fun map(rs: ResultSet, ctx: StatementContext): RefreshTokenEntity {
        val scopes = (rs.getArray("scopes").array as Array<String>)
            .map {
                ClientScope.fromString(it)
            }
            .toTypedArray()
            .toSet()
        return RefreshTokenEntity(
            id = UUID.fromString(rs.getString("id")),
            token = rs.getString("token"),
            subject = rs.getString("subject"),
            scopes = scopes,
            clientId = UUID.fromString(rs.getString("client_id")),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            usedAt = rs.getTimestamp("used_at")?.toInstant(),
            revokedAt = rs.getTimestamp("revoked_at")?.toInstant()
            )
    }
}
