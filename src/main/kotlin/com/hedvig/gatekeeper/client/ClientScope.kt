package com.hedvig.gatekeeper.client

import com.fasterxml.jackson.annotation.JsonCreator

enum class ClientScope(private val publicName: String) {
    MANAGE_EMPLOYEES("employees:manage"),
    MANAGE_MEMBERS("members:manage"),
    READ_MEMBERS("members:read"),
    READ_HOPE("hope:read"),
    WRITE_HOPE("hope:write"),
    PAYOUT("payments:payout"),
    CHARGE("payments:charge"),
    ADMIN_SYSTEM("system:admin")
    ;

    override fun toString(): String = publicName

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(string: String): ClientScope {
            val publicNames = values().map { value -> value.publicName }
            if (string !in publicNames) {
                throw InvalidClientScopeException("No such client scope \"$string\"")
            }

            return values()[publicNames.indexOf(string)]
        }
    }
}
