package com.hedvig.gatekeeper.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ClientScopeTest {
    @Test
    fun convertsToAndFromString() {
        val scope = ClientScope.MANAGE_MEMBERS
        assertThat(scope.toString()).isEqualTo("members/manage")
        assertThat(ClientScope.fromString("members/manage")).isEqualTo(ClientScope.MANAGE_MEMBERS)
    }

    @Test
    fun throwsErrorOnInvalidScope() {
        val scope = "not a valid scope"
        assertThrows<InvalidClientScopeException> {
            ClientScope.fromString(scope)
        }
    }
}