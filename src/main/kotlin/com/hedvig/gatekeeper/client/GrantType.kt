package com.hedvig.gatekeeper.client

import com.fasterxml.jackson.annotation.JsonValue

enum class GrantType(val publicName: String) {

    AUTHORIZATION_CODE("authorization_code"),
    PASSWORD("password"),
    REFRESH_TOKEN("refresh_token"),
    CLIENT_CREDENTIALS("client_credentials"),
    GOOGLE_SSO("google_sso");

    @JsonValue
    override fun toString(): String {
        return publicName
    }

    companion object {
        fun fromPublicString(publicString: String): GrantType {
            return when (publicString) {
                AUTHORIZATION_CODE.publicName -> AUTHORIZATION_CODE
                PASSWORD.publicName -> PASSWORD
                REFRESH_TOKEN.publicName -> REFRESH_TOKEN
                CLIENT_CREDENTIALS.publicName -> CLIENT_CREDENTIALS
                GOOGLE_SSO.publicName -> GOOGLE_SSO
                else ->
                    throw InvalidGrantTypeException("No such grant type \"$publicString\"")
            }
        }
    }
}
