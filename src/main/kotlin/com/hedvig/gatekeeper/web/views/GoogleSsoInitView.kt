package com.hedvig.gatekeeper.web.views

import com.hedvig.dropwizard.pebble.View

class GoogleSsoInitView(private val clientId: String) : View {
    override fun getContext(): Map<String, Any> = mapOf(
        "clientId" to clientId
    )

    override fun getName(): String = "sso/google-init.html.peb"
}