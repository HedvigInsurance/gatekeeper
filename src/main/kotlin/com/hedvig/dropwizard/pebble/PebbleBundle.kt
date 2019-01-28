package com.hedvig.dropwizard.pebble

import com.mitchellbosecke.pebble.PebbleEngine
import io.dropwizard.Bundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment

class PebbleBundle(
    private val prefix: String = "templates/",
    private val pebbleViewRenderer: PebbleEngine = PebbleEngine.Builder().build()
) : Bundle {
    override fun run(environment: Environment) {
        environment.jersey().register(PebbleMessageBodyWriter(pebbleViewRenderer, prefix))
    }

    override fun initialize(bootstrap: Bootstrap<*>) {
        // noop
    }
}
