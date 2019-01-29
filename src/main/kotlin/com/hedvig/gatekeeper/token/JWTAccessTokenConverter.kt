package com.hedvig.gatekeeper.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.converter.AccessTokenConverter
import java.time.Instant
import java.util.*

class JWTAccessTokenConverter(
    private val algorithm: Algorithm,
    private val getNow: () -> Instant,
    private val expirationTimeInSeconds: Long
) : AccessTokenConverter {
    override fun convertToToken(
        username: String?,
        clientId: String,
        requestedScopes: Set<String>,
        refreshToken: RefreshToken?
    ): AccessToken {
        val expires = getNow().plusSeconds(expirationTimeInSeconds)
        val jwt = JWT.create()
            .withSubject(username)
            .withJWTId(UUID.randomUUID().toString())
            .withAudience(clientId)
            .withArrayClaim("scopes", requestedScopes.toTypedArray())
            .withIssuedAt(Date.from(getNow()))
            .withExpiresAt(Date.from(expires))
            .withIssuer("com.hedvig.gatekeeper")
            .sign(algorithm)

        return AccessToken(
            accessToken = jwt,
            username = username,
            clientId = clientId,
            scopes = requestedScopes,
            expireTime = expires,
            refreshToken = refreshToken,
            tokenType = "jwt"
        )
    }
}
