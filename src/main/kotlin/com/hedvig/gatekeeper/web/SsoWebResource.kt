package com.hedvig.gatekeeper.web

import com.hedvig.dropwizard.pebble.View
import com.hedvig.gatekeeper.web.views.GoogleSsoInitView
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/sso")
class SsoWebResource {
    @GET
    @Produces(MediaType.TEXT_HTML)
    fun getSsoTemplate(): View {
        return GoogleSsoInitView()
    }
}
