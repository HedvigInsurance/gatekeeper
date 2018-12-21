package com.hedvig.gatekeeper.client

enum class ClientScope {
    ROOT,
    IEX;

    companion object {
        fun fromString(string: String): ClientScope {
            if (string == "ROOT") {
                return ROOT
            }
            if (string == "IEX") {
                return IEX
            }

            throw InvalidClientScopeException("No such client scope \"$string\"")
        }
    }
}
