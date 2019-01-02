package com.hedvig.gatekeeper.api

import com.hedvig.gatekeeper.api.dto.ClientDto
import com.hedvig.gatekeeper.client.ClientManager
import java.net.URI
import java.util.*
import javax.annotation.security.RolesAllowed
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/admin/clients")
@Produces(MediaType.APPLICATION_JSON)
class ClientResource(
    private val clientManager: ClientManager
) {
    @GET
    @RolesAllowed("ROOT")
    fun getClients(): Array<ClientDto> {
        return clientManager.findAll().map { ClientDto.fromClientEntity(it) }.toTypedArray()
    }

    @GET
    @Path("/{clientId}")
    @RolesAllowed("ROOT")
    fun getClient(@PathParam("clientId") clientId: UUID): ClientDto {
        val result = clientManager.find(clientId).map { ClientDto.fromClientEntity(it) }
        if (result.isEmpty) {
            throw BadRequestException("No such client")
        }

        return result.get()
    }

    @POST
    @RolesAllowed("ROOT")
    fun createCliet(@NotNull @Valid request: CreateClientRequestDto): Response {
        val result = clientManager.create(request, "TODO")
        return Response.created(URI.create("/admin/clients/${result.clientId}"))
            .entity(ClientDto.fromClientEntity(result))
            .build()
    }
}
