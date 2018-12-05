package com.hedvig.gatekeeper.api

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.hedvig.gatekeeper.api.dto.IssueAdminAccessTokenResponse
import com.hedvig.gatekeeper.api.dto.TokenIntrospectionResponse
import com.hedvig.gatekeeper.auth.*
import io.dropwizard.validation.OneOf
import javax.validation.constraints.NotNull
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/token")
@Produces(MediaType.APPLICATION_JSON)
class AuthIssuerResource(
    private val allowedGrantTypes: Array<GrantType>,
    private val grantTypeUserProvider: GrantTypeUserProvider,
    private val tokenIssuer: AccessTokenIssuer
) {
    @POST
    @Path("/internal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun issueAdminAccessToken(
        @NotNull
        @OneOf(
            value = ["dangerously_skip_user_verification"],
            message = "invalid"
        )
        @FormParam("grant_type")
        grantTypeInput: String,

        @FormParam("subject")
        subjectInput: String
    ): IssueAdminAccessTokenResponse {
        val grantType = GrantType.fromPublicName(grantTypeInput)
        if (!allowedGrantTypes.contains(grantType)) {
            throw BadRequestException("form field grant_type invalid")
        }

        val subject = (grantTypeUserProvider.getUserProvider(grantType) as UserProvider<String>).getSubjectFrom(subjectInput)
        val ctx = AccessTokenContext(subject = subject, audience = arrayOf("hedvig-gatekeeper"), roles = arrayOf(Role.ROOT))
        val accessToken = tokenIssuer.buildTokenFrom(ctx).sign(Algorithm.HMAC256("very secure"))

        return IssueAdminAccessTokenResponse(
            accessToken = accessToken,
            roles = arrayOf(Role.ROOT)
        )
    }

    @POST
    @Path("/internal/introspect")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun introspectToken(
        @NotNull @FormParam("token") token: String
    ): TokenIntrospectionResponse {
        try {
            val decodedToken = tokenIssuer.introspect(token, algorithm = Algorithm.HMAC256("very secure"))
            return TokenIntrospectionResponse(
                roles = decodedToken.getClaim("roles").asArray(String::class.java),
                subject = decodedToken.subject,
                expires = decodedToken.expiresAt.toInstant().epochSecond
            )
        } catch (e: JWTVerificationException) {
            throw ForbiddenException("invalid token")
        }
    }
}
