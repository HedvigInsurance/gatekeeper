package com.hedvig.gatekeeper.web.sso

import com.hedvig.dropwizard.errors.UnhandledErrorMessages
import com.hedvig.dropwizard.pebble.View
import com.hedvig.gatekeeper.web.sso.views.GoogleSSoSignoutView
import com.hedvig.gatekeeper.web.sso.views.GoogleSsoInitView
import com.hedvig.gatekeeper.web.sso.views.InvalidRedirectView
import com.hedvig.gatekeeper.web.sso.views.RedirectingView
import nl.myndocs.oauth2.exception.InvalidGrantException
import java.net.URI
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.NewCookie
import javax.ws.rs.core.Response

@Path("/sso")
@UnhandledErrorMessages
class SsoWebResource(
    private val googleWebClientId: String,
    private val redirectValidator: RedirectValidator,
    private val secureCookies: Boolean,
    private val cookieDomain: String,
    private val oauth2Client: Oauth2Client
) {
    @GET
    @Produces(MediaType.TEXT_HTML)
    fun getSsoTemplate(
        @QueryParam("redirect") redirect: String?,
        @QueryParam("error") error: String?
    ): View {
        return validateRedirectHost(redirect) ?: GoogleSsoInitView(googleWebClientId, redirect!!, error ?: "")
    }

    @GET
    @Path("signout")
    @Produces(MediaType.TEXT_HTML)
    fun getSsoSignoutTemplate(): View {
        return GoogleSSoSignoutView(googleWebClientId)
    }

    @GET
    @Path("/callback/google")
    @Produces(MediaType.TEXT_HTML)
    fun handleCallback(
        @NotNull
        @Valid
        @QueryParam("id_token")
        idToken: String,

        @QueryParam("redirect")
        redirect: String?
    ): Response {
        val validHostResult = validateRedirectHost(redirect)

        if (validHostResult != null) {
            return Response.ok().entity(validHostResult).build()
        }

        return try {
            val redirectUri = URI.create(redirect)
            val result = oauth2Client.grantGoogleSso(idToken)

            val atCookie = NewCookie(
                "hv_at",
                result.accessToken,
                "/",
                cookieDomain,
                "",
                result.expiresIn,
                secureCookies,
                true
            )
            val rtCookie = NewCookie(
                "hv_rt",
                result.refreshToken,
                "/",
                cookieDomain,
                "",
                3600 * 24 * 7,
                secureCookies,
                true
            )

            Response
                .temporaryRedirect(redirectUri)
                .cookie(atCookie)
                .cookie(rtCookie)
                .entity(RedirectingView())
                .build()
        } catch (e: InvalidGrantException) {
            val redirectQuery = redirect?.let { "&redirect=$it" } ?: ""

            Response
                .temporaryRedirect(URI.create("/sso?error=invalid_grant$redirectQuery"))
                .build()
        }
    }

    private fun validateRedirectHost(redirect: String?): View? {
        if (redirect == null || redirect == "") {
            return InvalidRedirectView("A redirect must be provided")
        }

        try {
            if (!redirectValidator.isValidHost(redirect)) {
                return InvalidRedirectView(URI.create(redirect).host + " is not a valid redirect domain")
            }
        } catch (e: Exception) {
            return InvalidRedirectView("Redirect must be a valid URI")
        }

        return null
    }
}
