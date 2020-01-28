package com.hedvig.gatekeeper.authorization.employees

import com.hedvig.gatekeeper.testhelp.JdbiTestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EmployeeRepositoryTest {
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
        val repository = EmployeeRepository(jdbiTestHelper.jdbi)

        repository.newEmployee("foo@hedvig.com")

        val result = repository.findByEmail("foo@hedvig.com")
        assertThat(result?.email).isEqualTo("foo@hedvig.com")
    }

    @Test
    fun doesntCreateExistingEmployee() {
        val repository = EmployeeRepository(jdbiTestHelper.jdbi)

        repository.newEmployee("foo@hedvig.com")
        assertThrows<EmployeeExistsException> {
            repository.newEmployee("foo@hedvig.com")
        }
    }
}
