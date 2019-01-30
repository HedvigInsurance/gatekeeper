package com.hedvig.gatekeeper.web.sso

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RedirectValidatorTest {
    @Test
    fun validatesValidHost() {
        val redirectValidator = RedirectValidator(setOf("hedvig.com"))

        assertThat(redirectValidator.isValidHost("https://hedvig.com")).isTrue()
    }

    @Test
    fun validatesInvalidHost() {
        val redirectValidator = RedirectValidator(setOf("hedvig.com"))

        assertThat(redirectValidator.isValidHost("https://not-hedvig.com")).isFalse()
        assertThat(redirectValidator.isValidHost("")).isFalse()
        assertThrows<RuntimeException> {
            redirectValidator.isValidHost("not a valid url")
        }
    }
}
