package com.hedvig.gatekeeper.authorization.employees

import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface EmployeeDao {
    @SqlUpdate("""
        INSERT INTO "employees" ("id", "email", "role", "first_granted_at")
        VALUES (:id, :email, :role, :firstGrantedAt)
        ;
    """)
    fun insert(@BindBean employee: Employee)

    @SqlQuery("""SELECT * FROM "employees" WHERE "email" = :email AND "deleted_at" IS NULL;""")
    fun findByEmail(@Bind("email") email: String): Optional<Employee>
}
