package com.hedvig.gatekeeper.api

import com.auth0.jwt.algorithms.Algorithm
import com.hedvig.gatekeeper.api.dto.TokenIntrospectionResponse
import com.hedvig.gatekeeper.auth.*
import io.dropwizard.jackson.Jackson
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form

@ExtendWith(DropwizardExtensionsSupport::class)
internal class AuthIssuerResourceTests {
    private val allowedGrantTypes = arrayOf(GrantType.DANGEROUSLY_SKIP_USER_VERIFICATION)
    private val resources = ResourceExtension.builder()
        .addResource(AuthIssuerResource(allowedGrantTypes, GrantTypeUserProvider(), AccessTokenIssuer()))
        .setMapper(Jackson.newMinimalObjectMapper())
        .build()

    @Test
    fun testIssuesAdminAccessTokenWithSkipUserVerification() {
        val formData = Form()
            .param("grant_type", "dangerously_skip_user_verification")
            .param("subject", "blargh@blargh.com")
        val response = resources.target("/token/internal").request().post(Entity.form(formData))

        assertEquals(200, response.status)
    }

    @Test
    fun testDoesntAcceptNonValidGrantType() {
        val formData = Form()
            .param("grant_type", "not a valid grant type")
            .param("subject", "blargh@blargh.com")
        val response = resources.target("/token/internal").request().post(Entity.form(formData))

        assertEquals(400, response.status)
    }

    @Test
    fun testIntrospectsValidToken() {
        val ctx = AccessTokenContext(
            subject = "foo@hedvig.com",
            roles = arrayOf(Role.ROOT)
        )
        val time = Instant.now()
        val token = AccessTokenIssuer().buildTokenFrom(ctx, time).sign(Algorithm.HMAC256("very secure"))

        val formData = Form().param("token", token)
        val response = resources.target("/token/internal/introspect").request().post(Entity.form(formData))

        assertEquals(200, response.status)

        val entity = response.readEntity(TokenIntrospectionResponse::class.java)
        assertEquals(entity.expires, time.plusSeconds(60 * 30).epochSecond)
        assertTrue(entity.roles.contentEquals(arrayOf("ROOT")))
        assertEquals(entity.subject, "foo@hedvig.com")
    }

    @Test
    fun testFailsToIntrospectExpiredToken() {
        val ctx = AccessTokenContext(
            subject = "foo@hedvig.com",
            roles = arrayOf(Role.ROOT)
        )
        val time = Instant.now().minusSeconds(60 * 30 + 1)
        val token = AccessTokenIssuer().buildTokenFrom(ctx, time).sign(Algorithm.HMAC256("very secure"))

        val formData = Form().param("token", token)
        val response = resources.target("/token/internal/introspect").request().post(Entity.form(formData))

        assertEquals(403, response.status)
    }
}
