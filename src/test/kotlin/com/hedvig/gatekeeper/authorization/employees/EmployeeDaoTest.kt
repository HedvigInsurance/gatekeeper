package com.hedvig.gatekeeper.authorization.employees

import com.hedvig.gatekeeper.db.JdbiConnector
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class EmployeeDaoTest {
    @Test
    fun createsAndFindsEmployee() {
        val jdbi = JdbiConnector.createForTest()
        jdbi.useHandle<RuntimeException> {
            it.execute("TRUNCATE employees;")
        }

        val employeeDao = jdbi.onDemand(EmployeeDao::class.java)

        val employee = Employee(
            id = UUID.randomUUID(),
            email = "foo@hedvig.com",
            role = Role.ROOT,
            firstGrantedAt = Instant.now()
        )

        employeeDao.insert(employee)

        val result = employeeDao.findByEmail(employee.email).get()
        assertThat(result).isEqualTo(employee)
    }
}