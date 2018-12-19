package com.hedvig.gatekeeper.client.persistence

import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.GrantType
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.*

class ClientRowMapper : RowMapper<ClientEntity> {
    override fun map(rs: ResultSet, ctx: StatementContext): ClientEntity {
        val scopes = (rs.getArray("authorized_scopes").array as Array<String>)
            .map {
                ClientScope.fromString(it)
            }
            .toTypedArray()
        val grantTypes = (rs.getArray("authorized_grant_types").array as Array<String>)
            .map {
                GrantType.fromPublicString(it)
            }
            .toTypedArray()
        val redirectUris = rs.getArray("redirect_uris").array as Array<String>
        return ClientEntity(
            clientId = UUID.fromString(rs.getString("client_id")),
            clientSecret = rs.getString("client_secret"),
            clientScopes = setOf(*scopes),
            authorizedGrantTypes = setOf(*grantTypes),
            redirectUris = setOf(*redirectUris),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            createdBy = rs.getString("created_by")
        )
    }
}
