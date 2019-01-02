package com.hedvig.gatekeeper.api

import com.hedvig.gatekeeper.security.User
import io.dropwizard.auth.Auth
import nl.myndocs.oauth2.config.Configuration
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.request.CallContext
import org.apache.log4j.LogManager.getLogger
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.*

object Oauth2Server {
    fun configure(configurationCallback: ConfigurationBuilder.Configuration.() -> Unit): Oauth2Resource {
        val defaultConfigurationCallback: ConfigurationBuilder.Configuration.() -> Unit = {
            tokenEndpoint = "oauth2/token"
            authorizationEndpoint = "oauth2/authorize"
            tokenInfoEndpoint = "oauth2/tokeninfo"
        }
        val configuration = ConfigurationBuilder.build {
            defaultConfigurationCallback(this)
            configurationCallback(this)
        }

        return Oauth2Resource(configuration)
    }
}

@Path("/oauth2")
@Produces(MediaType.APPLICATION_JSON)
class Oauth2Resource(private val configuration: Configuration) {
    private val LOG = getLogger(Oauth2Resource::class.java)

    @GET
    @Path("/authorize")
    fun authorize(
        @Context request: ContainerRequestContext,
        @Auth user: Optional<User>,
        @Context uriInfo: UriInfo
    ): Response {
        return route(request, uriInfo, mapOf())
    }

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun postToken(
        @Context request: ContainerRequestContext,
        @Context uriInfo: UriInfo,
        @Auth user: Optional<User>,
        requestBody: MultivaluedMap<String, String>
    ): Response {
        return route(
            request,
            uriInfo,
            mapOf(*requestBody.map { Pair(it.key, it.value.first()) }.toTypedArray())
        )
    }

    @GET
    @Path("/tokeninfo")
    fun getTokenInfo(
        @Context request: ContainerRequestContext,
        @Auth user: Optional<User>,
        @Context uriInfo: UriInfo
    ): Response {
        return route(request, uriInfo, mapOf())
    }

    private fun route(
        request: ContainerRequestContext,
        uriInfo: UriInfo,
        requestBody: Map<String, String>
    ): Response {
        val responseMaybe = ResponseMaybe()
        val callContext = JerseyCallContext(responseMaybe, request, uriInfo, requestBody)
        try {
            configuration.callRouter.route(callContext, configuration.authorizerFactory(callContext))
        } catch (e: Exception) {
            when (e) {
                is InvalidGrantException,
                is InvalidClientException -> {
                    LOG.warn("Received some kind of invalid token", e)
                    throw WebApplicationException(400)
                }
                else -> throw e
            }
        }
        val status = responseMaybe.status
        if (responseMaybe.value == null && status != null && status >= 400) {
            throw WebApplicationException(status)
        }

        val responseBuilder = Response.ok()
        if (status != null) {
            responseBuilder.status(status)
        }
        if (responseMaybe.value != null) {
            responseBuilder.entity(responseMaybe.value)
        }
        responseMaybe.headers.forEach { responseBuilder.header(it.key, it.value) }

        return responseBuilder.build()
    }
}

private class ResponseMaybe {
    var value: Any? = null
    var status: Int? = null
    val headers: MutableMap<String, String> = mutableMapOf()
}

private class JerseyCallContext(
    val responseMaybe: ResponseMaybe,

    requestContext: ContainerRequestContext,
    uriInfo: UriInfo,
    formParams: Map<String, String>?
) : CallContext {
    override val formParameters = formParams ?: mapOf()
    override val headers = mapOf(
        *requestContext.headers
            .map { Pair(it.key, it.value.last()) }
            .toTypedArray()
    )
    override val method = requestContext.request.method!!
    override val path = uriInfo.path
    override val queryParameters = mapOf(
        *uriInfo.queryParameters
            .map { Pair(it.key, it.value.toString()) }
            .toTypedArray()
    )

    override fun redirect(uri: String) {
        responseMaybe.status = 302
        responseMaybe.headers["Location"] = uri
    }

    override fun respondHeader(name: String, value: String) {
        responseMaybe.headers[name] = value
    }

    override fun respondJson(content: Any) {
        responseMaybe.value = content
    }

    override fun respondStatus(statusCode: Int) {
        responseMaybe.status = statusCode
    }
}
