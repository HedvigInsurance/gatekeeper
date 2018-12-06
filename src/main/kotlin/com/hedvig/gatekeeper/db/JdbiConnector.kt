package com.hedvig.gatekeeper.db

import com.hedvig.gatekeeper.GatekeeperConfiguration
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Environment
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.Query
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import java.lang.Exception
import java.lang.RuntimeException

class JdbiConnector {
    companion object {
        val logger: Logger = getLogger(JdbiConnector::class.java)
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
                    return JdbiFactory().build(environment, configuration.dataSourceFactory, "postgresql")
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