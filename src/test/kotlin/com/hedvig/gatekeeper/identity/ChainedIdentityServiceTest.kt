package com.hedvig.gatekeeper.identity

import io.mockk.every
import io.mockk.mockk
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test

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

        val identityServiceA = mockk<IdentityService>()
        val identityServiceB = mockk<IdentityService>()

        every { identityServiceA.allowedScopes(client, identity, requestedScopes) } returns scopesA
        every { identityServiceB.allowedScopes(client, identity, requestedScopes) } returns scopesB

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

        val identityServiceA = mockk<IdentityService>()
        val identityServiceB = mockk<IdentityService>()
        val identityServiceC = mockk<IdentityService>()

        every { identityServiceA.identityOf(client, "user-foo") } returns null
        every { identityServiceB.identityOf(client, "user-foo") } returns identity

        val chainedIdentityService =
            ChainedIdentityService(arrayOf(identityServiceA, identityServiceB, identityServiceC))

        assertEquals(identity, chainedIdentityService.identityOf(client, "user-foo"))
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

        val identityServiceA = mockk<IdentityService>()
        val identityServiceB = mockk<IdentityService>()
        val identityServiceC = mockk<IdentityService>()

        every { identityServiceA.validCredentials(client, identity, "pass") } returns false
        every { identityServiceB.validCredentials(client, identity, "pass") } returns true

        val chainedIdentityService =
            ChainedIdentityService(arrayOf(identityServiceA, identityServiceB, identityServiceC))

        assertTrue(chainedIdentityService.validCredentials(client, identity, "pass"))
    }
}
