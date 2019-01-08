package com.hedvig.gatekeeper.security

import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter
import io.dropwizard.testing.junit5.ResourceExtension
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory

class MockSecurityConfigurer(
    private val authenticatorRoles: Set<String> = setOf(),
    private val authorizerRoles: Set<String> = setOf()
) {
    fun configureMockSecurity(builder: ResourceExtension.Builder): ResourceExtension.Builder {
        return builder
            .setTestContainerFactory(GrizzlyWebTestContainerFactory())
            .addProvider(
                AuthDynamicFeature(
                    OAuthCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(MockAuthenticator(authenticatorRoles))
                        .setAuthorizer(MockAuthorizer(authorizerRoles))
                        .setPrefix("Bearer")
                        .buildAuthFilter()
                )
            )
            .addProvider(RolesAllowedDynamicFeature::class.java)
            .addProvider(AuthValueFactoryProvider.Binder<User>(User::class.java))
    }
}
