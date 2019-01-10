package com.hedvig.dropwizard.pebble

interface View {
    fun getContext(): Map<String, Any>? = emptyMap()
    fun getName(): String
}
