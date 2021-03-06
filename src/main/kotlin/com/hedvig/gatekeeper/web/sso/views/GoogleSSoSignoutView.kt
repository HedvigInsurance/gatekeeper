package com.hedvig.gatekeeper.web.sso.views

import com.hedvig.dropwizard.pebble.View

class GoogleSSoSignoutView(private val googleWebClientId: String) : View {
    override fun getContext(): Map<String, Any> = mapOf(
        "clientId" to googleWebClientId
    )

    override fun getName(): String = "sso/google-signout.html.peb"
}
