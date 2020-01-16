package com.hedvig.gatekeeper.identity

import com.hedvig.gatekeeper.authorization.employees.Employee
import com.hedvig.gatekeeper.authorization.employees.EmployeeDao
import com.hedvig.gatekeeper.authorization.employees.Role
import com.hedvig.gatekeeper.client.ClientScope
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.Instant
import java.util.*

internal class EmployeeIdentityServiceTest {
    @Test
    fun findsEmployeeAndIntersectsScopes() {
        val employeeManagerStub = mock(EmployeeDao::class.java)

        val employeeIdentityService = EmployeeIdentityService(employeeManagerStub)

        val client = Client(
            clientId = UUID.randomUUID().toString(),
            clientScopes = setOf(ClientScope.READ_HOPE.toString(), ClientScope.WRITE_HOPE.toString(), ClientScope.MANAGE_EMPLOYEES.toString()),
            authorizedGrantTypes = emptySet(),
            redirectUris = emptySet()
        )
        val employee = Employee(
            id = UUID.randomUUID(),
            email = "foo@bar.baz",
            role = Role.IEX,
            firstGrantedAt = Instant.now()
        )
        `when`(employeeManagerStub.findByEmail("foo@bar.baz")).thenReturn(Optional.of(employee))

        val result = employeeIdentityService.allowedScopes(
            client,
            Identity(employee.email),
            setOf(ClientScope.READ_HOPE.toString(), ClientScope.WRITE_HOPE.toString())
        )
        assertThat(result).isEqualTo(setOf(ClientScope.READ_HOPE.toString(), ClientScope.WRITE_HOPE.toString()))
    }

    @Test
    fun findsIdentityForEmployee() {
        val employeeManagerStub = mock(EmployeeDao::class.java)

        val employeeIdentityService = EmployeeIdentityService(employeeManagerStub)

        val client = Client(
            clientId = UUID.randomUUID().toString(),
            clientScopes = setOf(ClientScope.READ_HOPE.toString(), ClientScope.WRITE_HOPE.toString(), ClientScope.MANAGE_EMPLOYEES.toString()),
            authorizedGrantTypes = emptySet(),
            redirectUris = emptySet()
        )
        val employee = Employee(
            id = UUID.randomUUID(),
            email = "foo@bar.baz",
            role = Role.IEX,
            firstGrantedAt = Instant.now()
        )
        `when`(employeeManagerStub.findByEmail("foo@bar.baz")).thenReturn(Optional.of(employee))

        assertThat(employeeIdentityService.identityOf(client, employee.email)).isEqualTo(Identity(employee.email, mapOf("role" to Role.IEX)))
        assertThat(employeeIdentityService.identityOf(client, "not foo@bar.baz")).isNull()
    }

    @Test
    fun checksValidEmployee() {
        val employeeManagerStub = mock(EmployeeDao::class.java)

        val employeeIdentityService = EmployeeIdentityService(employeeManagerStub)

        val client = Client(
            clientId = UUID.randomUUID().toString(),
            clientScopes = setOf(ClientScope.READ_HOPE.toString(), ClientScope.WRITE_HOPE.toString(), ClientScope.MANAGE_EMPLOYEES.toString()),
            authorizedGrantTypes = emptySet(),
            redirectUris = emptySet()
        )
        val employee = Employee(
            id = UUID.randomUUID(),
            email = "foo@bar.baz",
            role = Role.IEX,
            firstGrantedAt = Instant.now()
        )
        `when`(employeeManagerStub.findByEmail("foo@bar.baz")).thenReturn(Optional.of(employee))

        assertThat(employeeIdentityService.validCredentials(client, Identity(employee.email), "blargh")).isTrue()
        assertThat(employeeIdentityService.validCredentials(client, Identity("not foo@bar.baz"), "blargh")).isFalse()
    }
}
