package com.hedvig.gatekeeper.health

import com.codahale.metrics.health.HealthCheck

class ApplicationHealthCheck : HealthCheck() {
    override fun check(): Result {
        return Result.healthy("We're all good here 👍")
    }
}