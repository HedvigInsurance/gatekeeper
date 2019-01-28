package com.hedvig.gatekeeper.authorization

import com.hedvig.gatekeeper.authorization.employees.Role
import com.hedvig.gatekeeper.client.ClientScope

class RoleScopeAssociator {
    fun getScopesFrom(role: Role): Set<ClientScope> {
        return when (role) {
            Role.NOBODY -> emptySet()
            Role.IEX -> setOf(ClientScope.READ_MEMBERS, ClientScope.MANAGE_MEMBERS, ClientScope.READ_HOPE, ClientScope.WRITE_HOPE)
            Role.IEX_EXTENDED -> this.getScopesFrom(Role.IEX) + setOf(ClientScope.MANAGE_PAYOUTS, ClientScope.REQUEST_PAYMENTS)
            Role.DEV -> setOf(ClientScope.READ_MEMBERS, ClientScope.MANAGE_MEMBERS, ClientScope.MANAGE_EMPLOYEES, ClientScope.READ_HOPE)
            Role.ROOT -> ClientScope.values().toSet()
        }
    }
}
