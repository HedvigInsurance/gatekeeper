package com.hedvig.gatekeeper.client.persistence

import com.hedvig.gatekeeper.api.CreateClientRequestDto
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.time.Instant
import java.util.*

interface ClientDao {
    @SqlQuery(
        """
    SELECT *
        FROM "clients"
        WHERE "client_id" = :clientId
            AND "deactivated_at" IS NULL;"""
    )
    @RegisterRowMapper(ClientRowMapper::class)
    fun find(
        @Bind("clientId") clientId: UUID
    ): Optional<ClientEntity>

    @SqlQuery(
        """
        SELECT *
            FROM "clients"
            WHERE "client_id" = :clientId
                AND "client_secret" = :clientSecret
                AND "deactivated_at" IS NULL;"""
    )
    @RegisterRowMapper(ClientRowMapper::class)
    fun findClientByIdAndSecret(
        @Bind("clientId") clientId: UUID,
        @Bind("clientSecret") clientSecret: String
    ): Optional<ClientEntity>

    @SqlQuery(
        """
    SELECT *
        FROM "clients"
        WHERE "deactivated_at" IS NULL
        ORDER BY "created_at" DESC
    ;"""
    )
    @RegisterRowMapper(ClientRowMapper::class)
    fun findAll(): Array<ClientEntity>

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

fun ClientDao.create(request: CreateClientRequestDto, createdBy: String): ClientEntity {
    val clientId = UUID.randomUUID()
    val clientSecret = UUID.randomUUID().toString()
    val clientEntity = ClientEntity(
        clientId = clientId,
        clientSecret = clientSecret,
        redirectUris = request.redirectUris,
        authorizedGrantTypes = request.authorizedGrantTypes,
        clientScopes = request.clientScopes,
        createdAt = Instant.now(),
        createdBy = createdBy
    )
    insert(clientEntity)
    return clientEntity
}
