package com.hedvig.gatekeeper.client.persistence

import com.hedvig.gatekeeper.api.CreateClientRequestDto
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.time.Instant
import java.util.*

@RegisterRowMapper(ClientRowMapper::class)
interface ClientDao {
    @SqlQuery(
        """
    SELECT *
        FROM "clients"
        WHERE "client_id" = :clientId
            AND "deactivated_at" IS NULL;"""
    )
    fun find(
        @Bind("clientId") clientId: UUID
    ): ClientEntity?

    @SqlQuery(
        """
        SELECT *
            FROM "clients"
            WHERE "client_id" = :clientId
                AND "client_secret" = :clientSecret
                AND "deactivated_at" IS NULL;"""
    )
    fun findClientByIdAndSecret(
        @Bind("clientId") clientId: UUID,
        @Bind("clientSecret") clientSecret: String
    ): ClientEntity?

    @SqlQuery(
        """
    SELECT *
        FROM "clients"
        WHERE "deactivated_at" IS NULL
        ORDER BY "created_at" DESC
    ;"""
    )
    fun findAll(): List<ClientEntity>

    @SqlUpdate("""
        INSERT INTO clients (
            client_id,
            client_secret,
            redirect_uris,
            authorized_grant_types,
            authorized_scopes,
            created_at,
            created_by
        )
        VALUES (
            :clientId,
            :clientSecret,
            :redirectUris,
            :authorizedGrantTypes,
            :clientScopes,
            :createdAt,
            :createdBy
        );"""
    )
    fun insert(@BindBean client: ClientEntity)
}
