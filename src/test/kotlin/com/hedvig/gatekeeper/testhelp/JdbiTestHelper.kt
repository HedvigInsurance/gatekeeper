package com.hedvig.gatekeeper.testhelp

import com.hedvig.gatekeeper.db.install
import com.hedvig.gatekeeper.testhelp.EmbeddedPostgresSingleton.embeddedPostgres
import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.DatabaseFactory.getInstance
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.LiquibaseException
import liquibase.resource.ClassLoaderResourceAccessor
import org.apache.commons.lang3.RandomStringUtils
import org.jdbi.v3.core.Jdbi
import java.sql.SQLException
import java.util.Locale
import javax.sql.DataSource

class JdbiTestHelper {
    private lateinit var _dataSource: DataSource
    private lateinit var _jdbi: Jdbi

    val dataSource: DataSource
        get() = _dataSource

    val jdbi: Jdbi
        get() = _jdbi

    fun before() {
        try {
            val dbName = RandomStringUtils.randomAlphabetic(12).toLowerCase(Locale.ENGLISH)

            val conn = embeddedPostgres.getDatabase("postgres", "").connection
            conn.autoCommit = true
            conn.createStatement().executeUpdate("""CREATE DATABASE "$dbName" OWNER postgres ENCODING = 'utf8';""")

            _dataSource = embeddedPostgres.getDatabase(
                "postgres",
                dbName
            )
            _jdbi = Jdbi.create(dataSource).install()

            val database = getInstance()
                .findCorrectDatabaseImplementation(JdbcConnection(dataSource.connection))
            Liquibase("migrations.xml", ClassLoaderResourceAccessor(), database)
                .update(Contexts())
        } catch (e: LiquibaseException) {
            throw SQLException(e)
        } finally {
            if (dataSource.connection != null) {
                if (!dataSource.connection.autoCommit) {
                    dataSource.connection.rollback()
                }
                dataSource.connection.close()
            }
        }
    }

    fun after() {
        dataSource.connection.close()
    }

    companion object {
        fun create(): JdbiTestHelper = JdbiTestHelper()
    }
}