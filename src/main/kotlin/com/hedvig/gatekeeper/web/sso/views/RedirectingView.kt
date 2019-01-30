package com.hedvig.gatekeeper.web.sso.views

import com.hedvig.dropwizard.pebble.View

class RedirectingView : View{
    override fun getName(): String = "sso/redirecting.html.peb"
}
