package com.hedvig.gatekeeper.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Instant
import java.util.*

class AccessTokenIssuer {
    fun buildTokenFrom(ctx: AccessTokenContext, time: Instant = Instant.now()): JWTCreator.Builder {
        return JWT.create()
            .withIssuer("hedvig-gatekeeper")
            .withAudience(*ctx.audience)
            .withIssuedAt(Date.from(time))
            .withExpiresAt(Date.from(time.plusSeconds(30L * 60L)))
            .withSubject(ctx.subject)
            .withArrayClaim("roles", ctx.roles.map { it.toString() }.toTypedArray())
    }

    @Throws(JWTVerificationException::class)
    fun introspect(
        token: String,
        expectedAudience: Array<String>? = null,
        algorithm: Algorithm
    ): DecodedJWT {
        val verification = JWT.require(algorithm)
            .withIssuer("hedvig-gatekeeper")
        if (expectedAudience != null) {
            verification.withAudience(*expectedAudience)
        }

        return verification
            .build()
            .verify(token)
    }
}
