package com.hedvig.gatekeeper.authorization.employees

import com.hedvig.gatekeeper.testhelp.JdbiTestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class EmployeeDaoTest {
    private val jdbiTestHelper = JdbiTestHelper.create()

    @BeforeEach
    fun before() {
        jdbiTestHelper.before()
    }

    @AfterEach
    fun after() {
        jdbiTestHelper.after()
    }

    @Test
    fun createsAndFindsEmployee() {
        val employeeDao = jdbiTestHelper.jdbi.onDemand(EmployeeDao::class.java)

        val employee = Employee(
            id = UUID.randomUUID(),
            email = "foo@hedvig.com",
            role = Role.ROOT,
            firstGrantedAt = Instant.now()
        )

        employeeDao.insert(employee)

        val result = employeeDao.findByEmail(employee.email)
        assertThat(result).isEqualTo(employee)
    }
}