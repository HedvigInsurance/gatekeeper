package com.hedvig.gatekeeper.oauth.persistence

import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import java.util.*

interface GrantDao {
    @SqlQuery("""SELECT * FROM "grants" WHERE "id" = :id;""")
    fun find(@Bind("id") id: UUID): Grant?

    @SqlUpdate("""
        INSERT INTO "grants" ("id", "subject", "grant_method", "client_id", "scopes", "granted_at")
        VALUES (:id, :subject, :grantMethod, :clientId, :scopes, :grantedAt)
    """)
    fun insert(@BindBean grant: Grant)

    companion object {
        val LOG: Logger = getLogger(GrantDao::class.java)
    }
}
