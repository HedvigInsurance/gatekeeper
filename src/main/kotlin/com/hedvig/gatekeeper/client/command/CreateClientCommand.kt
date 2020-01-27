package com.hedvig.gatekeeper.client.command

import com.hedvig.gatekeeper.GatekeeperConfiguration
import com.hedvig.gatekeeper.api.CreateClientRequestDto
import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.GrantType
import com.hedvig.gatekeeper.client.persistence.ClientDao
import com.hedvig.gatekeeper.client.persistence.create
import com.hedvig.gatekeeper.db.install
import io.dropwizard.cli.ConfiguredCommand
import io.dropwizard.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import org.jdbi.v3.core.Jdbi

class CreateClientCommand : ConfiguredCommand<GatekeeperConfiguration>("client-create", "create an Oauth client") {
    override fun run(bootstrap: Bootstrap<GatekeeperConfiguration>, namespace: Namespace, configuration: GatekeeperConfiguration) {
        val jdbi = Jdbi.create(
            configuration.dataSourceFactory.url,
            configuration.dataSourceFactory.user,
            configuration.dataSourceFactory.password
        ).install()

        val clientDao = jdbi.onDemand(ClientDao::class.java)

        val clientScopes = namespace.getList<String>("scopes")
            .map { ClientScope.fromString(it) }
            .toSet()
        val redirectUris = namespace.getList<String>("redirect-uris").toSet()
        val authorizedGrantTypes =
            namespace.getList<String>("authorized-grant-types")
                .map { GrantType.fromPublicString(it) }
                .toSet()

        val dto = CreateClientRequestDto(
            clientScopes = clientScopes,
            redirectUris = redirectUris,
            authorizedGrantTypes = authorizedGrantTypes
        )

        val client = clientDao.create(dto, "root")

        println("Client id: ${client.clientId}")
        println("Client secret: ${client.clientSecret}")
    }

    override fun configure(subparser: Subparser) {
        super.configure(subparser)

        subparser.addArgument("--scopes")
            .dest("scopes")
            .type(String::class.java)
            .nargs("+")
            .required(true)
            .help("Client scopes, valid values: ${ClientScope.values().joinToString(", ")}")

        subparser.addArgument("--redirect-uris")
            .dest("redirect-uris")
            .type(String::class.java)
            .nargs("+")
            .required(true)
            .help("Uris that the client can redirect to.")

        subparser.addArgument("--authorized-grant-types")
            .dest("authorized-grant-types")
            .type(String::class.java)
            .nargs("+")
            .required(true)
            .help("Grant types, valid values: ${GrantType.values().joinToString(", ")}")
    }
}