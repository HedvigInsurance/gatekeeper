package com.hedvig.gatekeeper.auth

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StaticSubjectProviderTests {

    @Test
    fun testGetSubjectFrom() {
        assertEquals("foo", StaticSubjectProvider().getSubjectFrom("foo"))
    }
}