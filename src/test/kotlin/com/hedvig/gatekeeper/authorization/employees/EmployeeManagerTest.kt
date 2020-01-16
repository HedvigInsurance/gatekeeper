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

        val dao = jdbi.onDemand(EmployeeDao::class.java)

        dao.newEmployee("foo@hedvig.com")

        val result = dao.findByEmail("foo@hedvig.com").get()
        assertThat(result.email).isEqualTo("foo@hedvig.com")
    }

    @Test
    fun doesntCreateExistingEmployee() {
        val jdbi = JdbiConnector.createForTest()
        jdbi.useHandle<RuntimeException> {
            it.execute("TRUNCATE employees;")
        }

        val dao = jdbi.onDemand(EmployeeDao::class.java)

        dao.newEmployee("foo@hedvig.com")
        assertThrows<EmployeeExistsException> {
            dao.newEmployee("foo@hedvig.com")
        }
    }
}
