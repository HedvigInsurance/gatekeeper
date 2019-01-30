package com.hedvig.gatekeeper.web.sso

import com.hedvig.dropwizard.errors.UnhandledErrorMessages
import com.hedvig.dropwizard.pebble.View
import com.hedvig.gatekeeper.web.sso.views.GoogleSSoSignoutView
import com.hedvig.gatekeeper.web.sso.views.GoogleSsoInitView
import nl.myndocs.oauth2.exception.InvalidGrantException
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
    private val googleWebClientId: String,
    private val oauth2Client: Oauth2Client
) {
    @GET
    @Produces(MediaType.TEXT_HTML)
    fun getSsoTemplate(@QueryParam("error") error: String?): View {
        return GoogleSsoInitView(googleWebClientId, error ?: "")
    }

    @GET
    @Path("signout")
    @Produces(MediaType.TEXT_HTML)
    fun getSsoSignoutTemplate(): View {
        return GoogleSSoSignoutView(googleWebClientId)
    }

    @GET
    @Path("/callback/google")
    @Produces(MediaType.APPLICATION_JSON)
    fun handleCallback(
        @NotNull
        @Valid
        @QueryParam("id_token")
        idToken: String,

        @QueryParam("redirect")
        redirect: String?
    ): Response {
        return try {
            val result = oauth2Client.grantGoogleSso(idToken)

            Response.ok()
                .entity(result)
                .build()
        } catch (e: InvalidGrantException) {
            val redirectQuery = redirect?.let { "&redirect=$it" } ?: ""

            Response
                .temporaryRedirect(URI.create("/sso?error=invalid_grant$redirectQuery"))
                .build()
        }
    }

}
