package com.hedvig.gatekeeper.db

import com.hedvig.gatekeeper.GatekeeperConfiguration
import com.hedvig.gatekeeper.client.ClientScope
import com.hedvig.gatekeeper.client.GrantType
import com.hedvig.gatekeeper.client.persistence.ClientScopeSetSqlargumentFactory
import com.hedvig.gatekeeper.client.persistence.EnumSetSqlArgumentFactory
import com.hedvig.gatekeeper.client.persistence.GrantTypeSetSqlArgumentFactory
import com.hedvig.gatekeeper.utils.DotenvFacade
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
import java.lang.Exception
import java.lang.RuntimeException
import java.util.*

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
                    return setupJdbi(JdbiFactory().build(environment, configuration.dataSourceFactory, "postgresql"))
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

        fun createForTest(): Jdbi {
            val jdbc = requireNotNull(DotenvFacade.getSingleton().getenv("DATABASE_TEST_JDBC")) { "Test JDBC cannot be null" }
            val user = requireNotNull(DotenvFacade.getSingleton().getenv("DATABASE_TEST_USER")) { "Database test user cannot be null" }
            val password = DotenvFacade.getSingleton().getenv("DATABASE_TEST_PASSWORD")

            val props = Properties()
            props["user"] = user
            if (password != null) {
                props["password"] = password
            }
            return setupJdbi(Jdbi.create(jdbc, props))
        }

        private fun setupJdbi(jdbi: Jdbi): Jdbi {
            return jdbi
                .installPlugin(SqlObjectPlugin())
                .installPlugin(PostgresPlugin())
                .installPlugin(KotlinPlugin())
                .installPlugin(KotlinSqlObjectPlugin())

                .registerArgument(GrantTypeSetSqlArgumentFactory())
                .registerArgument(ClientScopeSetSqlargumentFactory())
        }
    }
}