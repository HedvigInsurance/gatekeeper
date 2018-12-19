package com.hedvig.gatekeeper.client

import com.fasterxml.jackson.annotation.JsonValue

enum class GrantType(val publicName: String) {

    AUTHORIZATION_CODE("authorization_code"),
    PASSWORD("password");

    @JsonValue
    override fun toString(): String {
        return publicName
    }

    companion object {
        fun fromPublicString(publicString: String): GrantType {
            if (publicString == AUTHORIZATION_CODE.publicName) {
                return AUTHORIZATION_CODE
            }
            if (publicString == PASSWORD.publicName) {
                return PASSWORD
            }

            throw InvalidGrantTypeException("No such grant type \"$publicString\"")
        }
    }
}
