package com.hedvig.gatekeeper.web.sso

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.dropwizard.jackson.Jackson
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import nl.myndocs.oauth2.exception.InvalidGrantException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import javax.ws.rs.Consumes
import javax.ws.rs.FormParam
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@ExtendWith(DropwizardExtensionsSupport::class)
internal class Oauth2ClientTest {
    val resources = ResourceExtension.builder()
        .addResource(Oauth2EndpointResource())
        .setMapper(Jackson.newObjectMapper().registerModule(KotlinModule()))
        .build()

    @Test
    fun testGrantsGoogleSso() {
        val client = Oauth2Client(
            selfClientId = "id",
            selfClientSecret = "very secret",
            selfHost = "http://hedviglocaldev.com:8040",
            client = resources.client()
        )

        val result = client.grantGoogleSso("success")
        assertThat(result).isEqualTo(Oauth2Client.TokenResponse(
            accessToken = "at_success",
            refreshToken = "rt_success",
            tokenType = "jwt",
            expiresIn = 1799
        ))
    }

    @Test
    fun testFailsOnInvalidGrant() {
        val client = Oauth2Client(
            selfClientId = "id",
            selfClientSecret = "very secret",
            selfHost = "http://hedviglocaldev.com:8040",
            client = resources.client()
        )

        assertThrows<InvalidGrantException> {
            client.grantGoogleSso("not success")
        }
    }

    @Path("/oauth2/token")
    class Oauth2EndpointResource {
        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        fun getOauth2Response(@FormParam("google_id_token") idToken: String): Response {
            if (idToken == "success") {
                return Response.ok()
                    .header("Content-Type", "application/json")
                    .entity("""
                        {
                            "access_token": "at_success",
                            "refresh_token": "rt_success",
                            "token_type": "jwt",
                            "expires_in": 1799
                        }
                    """)
                    .build()
            }

            return Response.status(400)
                .build()
        }
    }
}