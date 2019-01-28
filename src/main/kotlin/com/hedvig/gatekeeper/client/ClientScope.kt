package com.hedvig.gatekeeper.client

enum class ClientScope {
    MANAGE_EMPLOYEES,
    MANAGE_MEMBERS,
    READ_MEMBERS,
    READ_HOPE,
    WRITE_HOPE,
    MANAGE_PAYOUTS,
    REQUEST_PAYMENTS
    ;

    companion object {
        fun fromString(string: String): ClientScope {
            return when (string) {
                "MANAGE_EMPLOYEES" -> MANAGE_EMPLOYEES
                "MANAGE_MEMBERS" -> MANAGE_MEMBERS
                "READ_MEMBERS" -> READ_MEMBERS
                "READ_HOPE" -> READ_HOPE
                "WRITE_HOPE"-> WRITE_HOPE
                "MANAGE_PAYOUTS" -> MANAGE_PAYOUTS
                "REQUEST_PAYMENTS" -> REQUEST_PAYMENTS
                else -> {
                    throw InvalidClientScopeException("No such client scope \"$string\"")
                }
            }
        }
    }
}
