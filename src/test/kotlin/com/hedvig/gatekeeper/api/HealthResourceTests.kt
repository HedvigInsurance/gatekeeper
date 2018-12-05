package com.hedvig.gatekeeper.api

import com.hedvig.gatekeeper.api.dto.PingResponse
import io.dropwizard.jackson.Jackson
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(DropwizardExtensionsSupport::class)
class HealthResourceTests {
    private val resources = ResourceExtension.builder()
        .addResource(HealthResource())
        .setMapper(Jackson.newMinimalObjectMapper())
        .build()
    @Test
    fun testPingPong() {
        val result = resources.target("/health/ping").request().get()
        assertEquals(200, result.status)
        assertTrue(result.readEntity(PingResponse::class.java).response.startsWith("Pong"))
    }
}