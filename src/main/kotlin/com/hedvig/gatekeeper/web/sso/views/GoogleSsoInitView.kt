package com.hedvig.gatekeeper.web.sso.views

import com.hedvig.dropwizard.pebble.View

class GoogleSsoInitView(
    private val clientId: String,
    private val redirect: String,
    private val error: String = ""
) : View {
    override fun getContext(): Map<String, Any> = mapOf(
        "clientId" to clientId,
        "redirect" to redirect,
        "error" to error
    )

    override fun getName(): String = "sso/google-init.html.peb"
}
