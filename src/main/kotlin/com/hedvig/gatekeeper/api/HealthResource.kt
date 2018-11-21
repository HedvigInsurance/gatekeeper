package com.hedvig.gatekeeper.api

import com.hedvig.gatekeeper.api.dto.PingResponse
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Path("/health")
class HealthResource {
    @GET
    @Path("/ping")
    fun getPing(): PingResponse {
        return PingResponse("Pong 🏓")
    }
}
