package com.hedvig.gatekeeper.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.Optional

internal class AccessTokenIssuerTests {
    @Test
    fun testBuildsAccessTokenWithoutExploding() {
        val issuer = AccessTokenIssuer()
        val time = Instant.now()
        val context = AccessTokenContext(
            subject = "blargh@hedvig.com",
            audience = arrayOf("hope"),
            roles = arrayOf(Role.ROOT)
        )
        val algorithm = Algorithm.HMAC256("very secure")
        val result = issuer.buildTokenFrom(context, time).sign(algorithm)
        val decodedResult = JWT.decode(result)

        assertEquals(context.subject, decodedResult.subject)
        val scopesResult = Optional.ofNullable(
            decodedResult.claims["roles"]
                ?.asArray(String::class.java)
                ?.map { Role.valueOf(it) }
                ?.toTypedArray()
        )
        assertTrue(scopesResult.map { it.contentEquals(context.roles) }.orElse(false))

        assertEquals(1, decodedResult.audience.size)
        assertTrue(decodedResult.audience.contains("hope"))

        assertEquals("hedvig-gatekeeper", decodedResult.issuer)
        assertEquals(
            time.plusSeconds(60 * 30).epochSecond,
            decodedResult.expiresAt.toInstant().epochSecond
        )
    }

    @Test
    fun testVerifiesValidAccessToken() {
        val issuer = AccessTokenIssuer()
        val time = Instant.now()
        val context = AccessTokenContext(
            subject = "blargh@hedvig.com",
            audience = arrayOf("hope"),
            roles = arrayOf(Role.ROOT)
        )
        val algorithm = Algorithm.HMAC256("very secure")
        val token = issuer.buildTokenFrom(context, time).sign(algorithm)
        val result = issuer.introspect(token = token, expectedAudience = arrayOf("hope"), algorithm = algorithm)
        assertEquals(context.subject, result.subject)
    }

    @Test()
    fun testVerifiesExpiredAccessToken() {
        val issuer = AccessTokenIssuer()
        val time = Instant.now().minusSeconds(30 * 60 + 1)
        val context = AccessTokenContext(
            subject = "blargh@hedvig.com",
            audience = arrayOf("hope"),
            roles = arrayOf(Role.ROOT)
        )
        val algorithm = Algorithm.HMAC256("very secure")
        val token = issuer.buildTokenFrom(context, time).sign(algorithm)
        assertThrows<JWTVerificationException> {
            issuer.introspect(token = token, expectedAudience = arrayOf("hope"), algorithm = algorithm)
        }
    }

    @Test()
    fun testVerifiesAccessTokenWithWrongAudience() {
        val issuer = AccessTokenIssuer()
        val time = Instant.now()
        val context = AccessTokenContext(
            subject = "blargh@hedvig.com",
            audience = arrayOf("hope"),
            roles = arrayOf(Role.ROOT)
        )
        val algorithm = Algorithm.HMAC256("very secure")
        val token = issuer.buildTokenFrom(context, time).sign(algorithm)
        assertThrows<JWTVerificationException> {
            issuer.introspect(token = token, expectedAudience = arrayOf("not hope"), algorithm = algorithm)
        }
    }

    @Test()
    fun testVerifiesAccessTokenWithWrongIssuer() {
        val issuer = AccessTokenIssuer()
        val time = Instant.now()
        val context = AccessTokenContext(
            subject = "blargh@hedvig.com",
            audience = arrayOf("hope"),
            roles = arrayOf(Role.ROOT)
        )
        val algorithm = Algorithm.HMAC256("very secure")
        val token = issuer.buildTokenFrom(context, time).withIssuer("not hedvig-gatekeeper").sign(algorithm)
        assertThrows<JWTVerificationException> {
            issuer.introspect(token = token, algorithm = algorithm)
        }
    }
}
