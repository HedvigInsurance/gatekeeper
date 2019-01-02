package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.utils.RandomGenerator
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.converter.RefreshTokenConverter
import org.apache.log4j.LogManager.getLogger
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class SecureRandomRefreshTokenConverter(
    private val randomGenerator: RandomGenerator,
    private val getNow: () -> Instant
) : RefreshTokenConverter {
    private val LOG = getLogger(RefreshTokenConverter::class.java)

    override fun convertToToken(username: String?, clientId: String, requestedScopes: Set<String>): RefreshToken {
        LOG.info("Making new refresh token [username=\"$username\"]")
        return RefreshToken(
            username = username,
            clientId = clientId,
            scopes = requestedScopes,
            refreshToken = Base64.getEncoder().encodeToString(randomGenerator.getBytes(512)),
            expireTime = getNow().plus(60, ChronoUnit.DAYS)
        )
    }
}
