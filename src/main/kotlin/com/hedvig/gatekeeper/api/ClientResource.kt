package com.hedvig.gatekeeper.api

import com.hedvig.gatekeeper.api.dto.ClientDto
import com.hedvig.gatekeeper.client.ClientManager
import com.hedvig.gatekeeper.security.User
import io.dropwizard.auth.Auth
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
    @RolesAllowed("ADMIN_SYSTEM")
    fun getClients(): Array<ClientDto> {
        return clientManager.findAll().map { ClientDto.fromClientEntity(it) }.toTypedArray()
    }

    @GET
    @Path("/{clientId}")
    @RolesAllowed("ADMIN_SYSTEM")
    fun getClient(@PathParam("clientId") clientId: UUID): ClientDto {
        val result = clientManager.find(clientId).map { ClientDto.fromClientEntity(it) }
        if (!result.isPresent) {
            throw BadRequestException("No such client")
        }

        return result.get()
    }

    @POST
    @RolesAllowed("ADMIN_SYSTEM")
    fun createClient(@NotNull @Valid request: CreateClientRequestDto, @Auth user: User): Response {
        val result = clientManager.create(request, user.name)
        return Response.created(URI.create("/admin/clients/${result.clientId}"))
            .entity(ClientDto.fromClientEntity(result))
            .build()
    }
}
