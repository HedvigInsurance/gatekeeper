package com.hedvig.gatekeeper.identity

import com.hedvig.gatekeeper.authorization.RoleScopeAssociator
import com.hedvig.gatekeeper.authorization.employees.EmployeeManager
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService

class EmployeeIdentityService(
    private val employeeManager: EmployeeManager,
    private val roleScopeAssociator: RoleScopeAssociator = RoleScopeAssociator()
) : IdentityService {
    override fun allowedScopes(forClient: Client, identity: Identity, scopes: Set<String>): Set<String> {
        val employee = employeeManager.findByEmail(identity.username)
        return roleScopeAssociator.getScopesFrom(employee.get().role).map { it.toString() }
            .intersect(forClient.clientScopes)
            .intersect(scopes)
    }

    override fun identityOf(forClient: Client, username: String): Identity? {
        return employeeManager.findByEmail(username).map { Identity(it.email) }.orElse(null)
    }

    override fun validCredentials(forClient: Client, identity: Identity, password: String): Boolean {
        return employeeManager.findByEmail(identity.username).isPresent
    }
}