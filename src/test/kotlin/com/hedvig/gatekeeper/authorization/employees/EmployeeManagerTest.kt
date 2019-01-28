package com.hedvig.gatekeeper.authorization.employees

import com.hedvig.gatekeeper.db.JdbiConnector
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EmployeeManagerTest {
    @Test
    fun createsAndFindsEmployee() {
        val jdbi = JdbiConnector.createForTest()
        jdbi.useHandle<RuntimeException> {
            it.execute("TRUNCATE employees;")
        }

        val employeeManager = jdbi.onDemand(EmployeeManager::class.java)

        employeeManager.newEmployee("foo@hedvig.com")

        val result = employeeManager.findByEmail("foo@hedvig.com").get()
        assertThat(result.email).isEqualTo("foo@hedvig.com")
    }

    @Test
    fun doesntCreateExistingEmployee() {
        val jdbi = JdbiConnector.createForTest()
        jdbi.useHandle<RuntimeException> {
            it.execute("TRUNCATE employees;")
        }

        val employeeManager = jdbi.onDemand(EmployeeManager::class.java)

        employeeManager.newEmployee("foo@hedvig.com")
        assertThrows<EmployeeExistsException> {
            employeeManager.newEmployee("foo@hedvig.com")
        }
    }
}
