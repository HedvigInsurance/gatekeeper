package com.hedvig.gatekeeper.web.views

import com.hedvig.dropwizard.pebble.View
import com.hedvig.gatekeeper.utils.DotenvFacade

class GoogleSSoSignoutView : View {
    override fun getContext(): Map<String, Any> = mapOf(
        "clientId" to DotenvFacade.getSingleton().getenv("GOOGLE_WEB_CLIENT_ID")!!
    )

    override fun getName(): String = "sso/google-logout.html.peb"
}
