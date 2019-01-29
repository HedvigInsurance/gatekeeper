package com.hedvig.gatekeeper.authorization.employees

import java.time.Instant
import java.util.*

data class Employee(
    val id: UUID,
    val email: String,
    val role: Role = Role.NOBODY,
    val firstGrantedAt: Instant,
    val deletedAt: Instant? = null
)
