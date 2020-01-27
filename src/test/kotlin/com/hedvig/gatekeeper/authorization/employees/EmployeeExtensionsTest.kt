package com.hedvig.gatekeeper.authorization.employees

import com.hedvig.gatekeeper.testhelp.JdbiTestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EmployeeExtensionsTest {
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
        val dao = jdbiTestHelper.jdbi.onDemand(EmployeeDao::class.java)

        dao.newEmployee("foo@hedvig.com")

        val result = dao.findByEmail("foo@hedvig.com").get()
        assertThat(result.email).isEqualTo("foo@hedvig.com")
    }

    @Test
    fun doesntCreateExistingEmployee() {
        val dao = jdbiTestHelper.jdbi.onDemand(EmployeeDao::class.java)

        dao.newEmployee("foo@hedvig.com")
        assertThrows<EmployeeExistsException> {
            dao.newEmployee("foo@hedvig.com")
        }
    }
}
