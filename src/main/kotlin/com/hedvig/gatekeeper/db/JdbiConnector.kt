package com.hedvig.gatekeeper.db

import com.hedvig.gatekeeper.GatekeeperConfiguration
import com.hedvig.gatekeeper.client.persistence.ClientScopeSetSqlargumentFactory
import com.hedvig.gatekeeper.client.persistence.GrantTypeSetSqlArgumentFactory
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Environment
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.core.statement.Query
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

class JdbiConnector {
    companion object {
        private val logger: Logger = getLogger(JdbiConnector::class.java)
        fun connect(configuration: GatekeeperConfiguration, environment: Environment): Jdbi {
            logger.info("Trying to connect to database")

            var attempts = 1
            while (attempts <= 30) {
                try {
                    logger.info("Trying to connect to db, waiting 1 second before attempt {}/30", attempts)
                    Thread.sleep(1_000)
                    val connectionTestJdbi = Jdbi.create(
                        configuration.dataSourceFactory.url,
                        configuration.dataSourceFactory.user,
                        configuration.dataSourceFactory.password
                    )
                    connectionTestJdbi.withHandle<Query, RuntimeException> { it.select("SELECT 1") }
                    logger.info("Successfully connected to and pinged database, setting up real connection");
                    return JdbiFactory()
                        .build(environment, configuration.dataSourceFactory, "postgresql")
                        .install()
                } catch (e: InterruptedException) {
                    logger.error("Thread interrupted while trying to connect to DB")
                    throw RuntimeException("Thread interrupted while trying to connect to DB", e)
                } catch (e: Exception) {
                    logger.warn("Failed to connect to db: \"{}\"", e.message)
                    attempts += 1
                }
            }

            logger.error("Finally cannot connect to DB after 30 attempts")
            throw RuntimeException("Finally cannot connect to DB")
        }
    }
}

fun Jdbi.install(): Jdbi {
    return this
        .installPlugin(SqlObjectPlugin())
        .installPlugin(PostgresPlugin())
        .installPlugin(KotlinPlugin())
        .installPlugin(KotlinSqlObjectPlugin())

        .registerArgument(GrantTypeSetSqlArgumentFactory())
        .registerArgument(ClientScopeSetSqlargumentFactory())
}
