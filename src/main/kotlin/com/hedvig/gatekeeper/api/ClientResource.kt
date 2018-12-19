package com.hedvig.gatekeeper.api

import com.hedvig.gatekeeper.api.dto.ClientDto
import com.hedvig.gatekeeper.client.ClientManager
import java.net.URI
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/admin/clients")
@Produces(MediaType.APPLICATION_JSON)
class ClientResource(
    private val clientManager: ClientManager
) {
    @GET
    fun getClients(): Array<ClientDto> {
        return clientManager.findAll().map { ClientDto.fromClientEntity(it) }.toTypedArray()
    }

    @POST
    fun createClient(@NotNull @Valid request: CreateClientRequestDto): Response {
        val result = clientManager.create(request, "TODO")
        return Response.created(URI.create("/admin/clients/${result.clientId}"))
            .entity(ClientDto.fromClientEntity(result))
            .build()
    }
}
