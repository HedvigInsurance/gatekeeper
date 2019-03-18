package com.hedvig.gatekeeper.token

import com.hedvig.gatekeeper.utils.RandomGenerator
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.converter.RefreshTokenConverter
import org.apache.log4j.LogManager.getLogger
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class SecureRandomRefreshTokenConverter(
    private val randomGenerator: RandomGenerator,
    private val getNow: () -> Instant,
    private val expiryTimeInDays: Long
) : RefreshTokenConverter {
    private val LOG = getLogger(RefreshTokenConverter::class.java)
    override fun convertToToken(identity: Identity?, clientId: String, requestedScopes: Set<String>): RefreshToken {
        LOG.debug("Making new refresh token [username=\"${identity?.username}\"]")
        return RefreshToken(
            identity = identity,
            clientId = clientId,
            scopes = requestedScopes,
            refreshToken = Base64.getEncoder().encodeToString(randomGenerator.getBytes(512)),
            expireTime = getNow().plus(expiryTimeInDays, ChronoUnit.DAYS)
        )
    }
}
