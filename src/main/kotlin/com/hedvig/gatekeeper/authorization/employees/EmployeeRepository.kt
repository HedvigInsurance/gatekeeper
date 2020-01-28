package com.hedvig.gatekeeper.authorization.employees

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import java.time.Instant
import java.util.*

class EmployeeRepository (private val jdbi: Jdbi) {
    fun insert(employee: Employee) {
        jdbi.useHandle<RuntimeException> { handle ->
            handle.attach<EmployeeDao>().insert(employee)
        }
    }

    fun findByEmail(email: String)  =
        jdbi.withHandle<Employee?, RuntimeException> { handle ->
            handle.attach<EmployeeDao>().findByEmail(email)
        }

    fun newEmployee(email: String): Employee {
        findByEmail(email)?.let {
            throw EmployeeExistsException()
        }

        val employee = Employee(
            id = UUID.randomUUID(),
            email = email,
            role = Role.NOBODY,
            firstGrantedAt = Instant.now()
        )
        insert(employee)

        return employee
    }
}