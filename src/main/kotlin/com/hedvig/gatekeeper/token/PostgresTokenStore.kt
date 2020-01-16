package com.hedvig.gatekeeper.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.oauth.persistence.GrantDao
import com.hedvig.gatekeeper.oauth.persistence.storeGrant
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.TokenStore
import org.apache.log4j.LogManager.getLogger
import java.time.temporal.ChronoUnit
import java.util.*

class PostgresTokenStore(
    private val refreshTokenDao: RefreshTokenDao,
    private val grantDao: GrantDao,
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
                clientId = jwt.audience[0],
                tokenType = "jwt",
                identity = Identity(username = jwt.subject),
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
        return refreshTokenDao.markAsUsed(token)
            .map {
                RefreshToken(
                    scopes = it.scopes.map { scope -> scope.toString() }.toSet(),
                    refreshToken = it.token,
                    clientId = it.clientId.toString(),
                    identity = Identity(username = it.subject),
                    expireTime = it.createdAt.plus(60, ChronoUnit.DAYS)
                )
            }
            .orElse(null)
    }

    override fun revokeAccessToken(token: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun revokeRefreshToken(token: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun storeAccessToken(accessToken: AccessToken) {
        grantDao.storeGrant(
            subject = accessToken.identity!!.username,
            clientId = UUID.fromString(accessToken.clientId),
            scopes = accessToken.scopes,
            grantMethod = "TODO"
        )
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
            LOG.info("Invalid uuid for client_id [client_id='${refreshToken.clientId}']")
            throw InvalidClientException()
        }
        refreshTokenDao.createRefreshToken(
            refreshToken.identity?.username ?: "system",
            clientId,
            refreshToken.scopes.map { ClientScope.fromString(it) }.toSet(),
            refreshToken.refreshToken
        )
    }
}
