package com.hedvig.gatekeeper.authorization.employees

import org.jdbi.v3.sqlobject.CreateSqlObject
import org.jdbi.v3.sqlobject.transaction.Transaction
import java.time.Instant
import java.util.*

interface EmployeeManager {
    @CreateSqlObject
    fun employeeDao(): EmployeeDao

    @Transaction
    fun newEmployee(email: String): Employee {
        if (employeeDao().findByEmail(email).isPresent) {
            throw EmployeeExistsException()
        }

        val employee = Employee(
            id = UUID.randomUUID(),
            email = email,
            role = Role.NOBODY,
            firstGrantedAt = Instant.now()
        )
        employeeDao().insert(employee)

        return employee
    }

    @Transaction
    fun findByEmail(email: String): Optional<Employee> {
        return employeeDao().findByEmail(email)
    }
}
