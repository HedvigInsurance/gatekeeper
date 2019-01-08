package com.hedvig.gatekeeper.client

enum class ClientScope {
    ROOT,
    IEX;

    companion object {
        fun fromString(string: String): ClientScope {
            return when (string) {
                "ROOT" -> ROOT
                "IEX" -> IEX
                else -> {
                    throw InvalidClientScopeException("No such client scope \"$string\"")
                }
            }
        }
    }
}
