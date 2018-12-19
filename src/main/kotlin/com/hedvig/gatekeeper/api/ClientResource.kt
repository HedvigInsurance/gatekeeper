package com.hedvig.gatekeeper.api

import com.hedvig.gatekeeper.api.dto.ClientDto
import com.hedvig.gatekeeper.client.ClientManager
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/admin/clients")
@Produces(MediaType.APPLICATION_JSON)
class ClientResource(
    private val clientManager: ClientManager
) {
    @GET
    fun getClients(): Array<ClientDto> {
        return clientManager.findAll().map { ClientDto.fromClientEntity(it) }.toTypedArray()
    }
}
