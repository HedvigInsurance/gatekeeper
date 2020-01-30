package com.hedvig.gatekeeper.web.sso

import java.net.URI

class RedirectValidator(private val validDomains: Set<String>) {
    fun isValidHost(redirect: String): Boolean {
        val uri = URI.create(redirect)

        // TODO also redirect port? Maybe even the exact uri?
        return validDomains.contains(uri.host)
    }
}
