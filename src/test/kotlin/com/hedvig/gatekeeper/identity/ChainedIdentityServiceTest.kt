package com.hedvig.gatekeeper.identity

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import org.junit.Assert.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

internal class ChainedIdentityServiceTest {
    @Test
    fun getsAllAllowedScopesFromClients() {
        val requestedScopes = setOf("BAR", "BAZ")
        val scopesA = setOf("FOO", "BAR")
        val scopesB = setOf("BAZ")
        val client = Client(
            clientId = "abc123",
            clientScopes = scopesA,
            authorizedGrantTypes = emptySet(),
            redirectUris = emptySet()
        )
        val identity = Identity("user-foo")

        val identityServiceA = mock(IdentityService::class.java)
        val identityServiceB = mock(IdentityService::class.java)

        `when`(identityServiceA.allowedScopes(client, identity, requestedScopes)).thenReturn(scopesA)
        `when`(identityServiceB.allowedScopes(client, identity, requestedScopes)).thenReturn(scopesB)

        val chainedIdentityService = ChainedIdentityService(arrayOf(identityServiceA, identityServiceB))
        val resultingScopes = chainedIdentityService.allowedScopes(client, identity, requestedScopes)

        assertArrayEquals(arrayOf("FOO", "BAR", "BAZ"), resultingScopes.toTypedArray())
    }

    @Test
    fun findsFirstIdentityAndStops() {
        val client = Client(
            clientId = "abc123",
            clientScopes = emptySet(),
            authorizedGrantTypes = emptySet(),
            redirectUris = emptySet()
        )
        val identity = Identity("user-foo")

        val identityServiceA = mock(IdentityService::class.java)
        val identityServiceB = mock(IdentityService::class.java)
        val identityServiceC = mock(IdentityService::class.java)

        `when`(identityServiceA.identityOf(client, "user-foo")).thenReturn(null)
        `when`(identityServiceB.identityOf(client, "user-foo")).thenReturn(identity)

        val chainedIdentityService = ChainedIdentityService(arrayOf(identityServiceA, identityServiceB, identityServiceC))

        assertEquals(identity, chainedIdentityService.identityOf(client, "user-foo"))

        verifyZeroInteractions(identityServiceC)
    }

    @Test
    fun testsValidCredentialsAndStops() {
        val client = Client(
            clientId = "abc123",
            clientScopes = emptySet(),
            authorizedGrantTypes = emptySet(),
            redirectUris = emptySet()
        )
        val identity = Identity("user-foo")

        val identityServiceA = mock(IdentityService::class.java)
        val identityServiceB = mock(IdentityService::class.java)
        val identityServiceC = mock(IdentityService::class.java)

        `when`(identityServiceA.validCredentials(client, identity, "pass")).thenReturn(false)
        `when`(identityServiceB.validCredentials(client, identity, "pass")).thenReturn(true)

        val chainedIdentityService = ChainedIdentityService(arrayOf(identityServiceA, identityServiceB, identityServiceC))

        assertTrue(chainedIdentityService.validCredentials(client, identity, "pass"))

        verifyZeroInteractions(identityServiceC)
    }
}
