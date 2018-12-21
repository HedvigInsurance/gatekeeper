package com.hedvig.gatekeeper.client.persistence

import org.jdbi.v3.core.argument.AbstractArgumentFactory
import org.jdbi.v3.core.argument.Argument
import org.jdbi.v3.core.config.ConfigRegistry
import org.jdbi.v3.core.statement.StatementContext
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Types

open class EnumSetSqlArgumentFactory<TEnum> : AbstractArgumentFactory<MutableSet<TEnum>>(Types.ARRAY) {
    override fun build(value: MutableSet<TEnum>, config: ConfigRegistry): Argument {
        return ArgumentLambdaHackWrapper { position: Int, statement: PreparedStatement, ctx: StatementContext ->
            statement.setArray(position,
                ctx.connection.createArrayOf(
                    "text",
                    value.map { it.toString() }.toTypedArray())
            )
        }
    }

    private class ArgumentLambdaHackWrapper(val fn: (Int, PreparedStatement, StatementContext) -> Unit) : Argument {
        @Throws(SQLException::class)
        override fun apply(position: Int, stmt: PreparedStatement, ctx: StatementContext) {
            fn(position, stmt, ctx)
        }
    }
}

