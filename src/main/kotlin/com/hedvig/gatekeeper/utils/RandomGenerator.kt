package com.hedvig.gatekeeper.utils

import java.security.SecureRandom

class RandomGenerator(val random: SecureRandom) {
    fun getBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes
    }
}
