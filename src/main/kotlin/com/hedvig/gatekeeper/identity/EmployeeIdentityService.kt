package com.hedvig.gatekeeper.identity

import com.hedvig.gatekeeper.authorization.RoleScopeAssociator
import com.hedvig.gatekeeper.authorization.employees.EmployeeDao
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import org.slf4j.LoggerFactory.getLogger

class EmployeeIdentityService(
    private val employeeDao: EmployeeDao,
    private val roleScopeAssociator: RoleScopeAssociator = RoleScopeAssociator()
) : IdentityService {
    private val LOG = getLogger(EmployeeIdentityService::class.java)

    override fun allowedScopes(forClient: Client, identity: Identity, scopes: Set<String>): Set<String> {
        val employee = employeeDao.findByEmail(identity.username)
        return roleScopeAssociator.getScopesFrom(employee.get().role).map { it.toString() }
            .intersect(forClient.clientScopes)
            .intersect(scopes)
    }

    override fun identityOf(forClient: Client, username: String): Identity? {
        val result = employeeDao.findByEmail(username)
            .map { employee -> Identity(employee.email, mapOf("role" to employee.role)) }
            .orElse(null)

        if (result == null) {
            LOG.warn("Couldn't authenticate employee [username='$username']")
        } else {
            LOG.info("Successfully authenticated employee [username='$username']")
        }

        return result
    }

    override fun validCredentials(forClient: Client, identity: Identity, password: String): Boolean {
        return employeeDao.findByEmail(identity.username).isPresent
    }
}