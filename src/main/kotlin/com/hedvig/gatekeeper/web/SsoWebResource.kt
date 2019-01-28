package com.hedvig.gatekeeper.web

import com.hedvig.dropwizard.errors.UnhandledErrorMessages
import com.hedvig.dropwizard.pebble.View
import com.hedvig.gatekeeper.web.views.GoogleSSoSignoutView
import com.hedvig.gatekeeper.web.views.GoogleSsoInitView
import java.net.URI
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder.newClient
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.Response

@Path("/sso")
@UnhandledErrorMessages
class SsoWebResource(
    private val selfClientId: String,
    private val selfClientSecret: String,
    private val googleWebClientId: String,
    private val client: Client = newClient()
) {
    @GET
    @Produces(MediaType.TEXT_HTML)
    fun getSsoTemplate(): View {
        return GoogleSsoInitView(googleWebClientId)
    }

    @GET
    @Path("logout")
    @Produces(MediaType.TEXT_HTML)
    fun getSsoSignoutTemplate(): View {
        return GoogleSSoSignoutView()
    }

    @GET
    @Path("/callback/google")
    @Produces(MediaType.TEXT_PLAIN)
    fun handleCallback(
        @NotNull
        @Valid
        @QueryParam("id_token")
        idToken: String
    ): Response {
        val uri = URI.create("http://localhost:8040/oauth2/token")
        val result = client.target(uri)
            .request()
            .post(Entity.form(MultivaluedHashMap(mapOf(
                "grant_type" to "google_sso",
                "client_id" to selfClientId,
                "client_secret" to selfClientSecret,
                "google_id_token" to idToken
            ))))
            .readEntity(String::class.java)
        return Response.ok()
            .entity(result)
            .build()
    }
}
