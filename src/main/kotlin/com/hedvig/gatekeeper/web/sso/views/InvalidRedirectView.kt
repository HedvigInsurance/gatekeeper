package com.hedvig.gatekeeper.web.sso.views

import com.hedvig.dropwizard.pebble.View

class InvalidRedirectView(private val error: String) : View {
    override fun getContext(): Map<String, Any> = mapOf("error" to error)

    override fun getName(): String = "sso/invalid-redirect.html.peb"
}
