package com.hedvig.gatekeeper.testhelp

import com.opentable.db.postgres.embedded.EmbeddedPostgres

object EmbeddedPostgresSingleton {
    private var _embeddedPostgres: EmbeddedPostgres? = null
    val embeddedPostgres: EmbeddedPostgres
        get() {
            if (_embeddedPostgres == null) {
                _embeddedPostgres = EmbeddedPostgres.builder()
                    .start()
            }

            return _embeddedPostgres!!
        }
}