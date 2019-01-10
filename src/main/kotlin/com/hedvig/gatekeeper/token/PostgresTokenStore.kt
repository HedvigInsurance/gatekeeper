package com.hedvig.gatekeeper.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.hedvig.gatekeeper.client.ClientScope
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.TokenStore
import org.apache.log4j.LogManager.getLogger
import java.time.temporal.ChronoUnit
import java.util.*

class PostgresTokenStore(
    private val refreshTokenManager: RefreshTokenManager,
    private val algorithm: Algorithm
) : TokenStore {
    private val LOG = getLogger(TokenStore::class.java)

    override fun accessToken(token: String): AccessToken? {
        try {
            LOG.info("Trying to decode access token")
            val jwt = JWT.require(algorithm)
                .withIssuer("com.hedvig.gatekeeper")
                .build()
                .verify(token)
            return AccessToken(
                accessToken = token,
                expireTime = jwt.expiresAt.toInstant(),
                clientId = jwt.getClaim("client_id").asString(),
                tokenType = "jwt",
                username = jwt.subject,
                scopes = jwt.getClaim("scopes").asArray(String::class.java).toSet(),
                refreshToken = null
            )
        } catch (e: JWTVerificationException) {
            LOG.warn("Invalid access token")
            return null
        } catch (e: JWTDecodeException) {
            LOG.warn("Malformatted access token")
            return null
        }
    }

    override fun codeToken(token: String): CodeToken? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun consumeCodeToken(token: String): CodeToken? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun refreshToken(token: String): RefreshToken? {
        LOG.info("Refreshing refresh token")
        return refreshTokenManager.markAsUsed(token).map { it ->
            RefreshToken(
                scopes = it.scopes.map { it.toString() }.toSet(),
                refreshToken = it.token,
                clientId = it.clientId.toString(),
                username = it.subject,
                expireTime = it.createdAt.plus(60, ChronoUnit.DAYS)
            )
        }.orElse(null)
    }

    override fun revokeAccessToken(token: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun revokeRefreshToken(token: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun storeAccessToken(accessToken: AccessToken) {
        val refreshToken = accessToken.refreshToken
        if (refreshToken != null) {
            storeRefreshToken(refreshToken)
        }
    }

    override fun storeCodeToken(codeToken: CodeToken) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun storeRefreshToken(refreshToken: RefreshToken) {
        LOG.info("Storing refresh token")
        val clientId = try {
            UUID.fromString(refreshToken.clientId)
        } catch (e: IllegalArgumentException) {
            throw InvalidClientException()
        }
        refreshTokenManager.createRefreshToken(
            refreshToken.username ?: "system",
            clientId,
            refreshToken.scopes.map { ClientScope.fromString(it) }.toSet(),
            refreshToken.refreshToken
        )
    }
}
