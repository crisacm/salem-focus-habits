/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.crisacm.salem.platform.io

import co.touchlab.sqliter.Cursor
import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.DatabaseConnection
import co.touchlab.sqliter.JournalMode
import co.touchlab.sqliter.Statement
import co.touchlab.sqliter.createDatabaseManager
import co.touchlab.sqliter.getVersion
import platform.Foundation.NSUUID

/**
 * [Database] for iOS on top of sqliter. It follows the behaviour of `JavaDatabase`: typed binds,
 * `step()` that stays DONE once the result set is exhausted, and `BEGIN`/`COMMIT` statements that
 * only start and end a real transaction at the outermost level (`ROLLBACK` ends it at any level).
 */
class SqliterDatabase internal constructor(private val connection: DatabaseConnection) : Database {
    private var transactionDepth = 0
    private val statements = mutableSetOf<SqliterPreparedStatement>()

    override fun prepareStatement(sql: String): PreparedStatement {
        val trimmed = sql.trimStart().uppercase()
        if (trimmed.startsWith("BEGIN")) return TransactionStatement(::beginTransaction)
        if (trimmed.startsWith("COMMIT")) return TransactionStatement(::commitTransaction)
        if (trimmed.startsWith("ROLLBACK")) return TransactionStatement(::rollbackTransaction)
        val statement = SqliterPreparedStatement(connection.createStatement(sql)) { statements.remove(it) }
        statements.add(statement)
        return statement
    }

    override fun close() {
        if (connection.closed) return
        // An unfinalized statement would make sqlite refuse to close the connection
        for (statement in statements.toList()) statement.finalize()
        connection.close()
    }

    private fun beginTransaction() {
        if (transactionDepth == 0) connection.rawExecSql("BEGIN")
        transactionDepth++
    }

    private fun commitTransaction() {
        // A COMMIT without a matching BEGIN is ignored
        if (transactionDepth == 0) return
        transactionDepth--
        if (transactionDepth == 0) connection.rawExecSql("COMMIT")
    }

    // A rollback ends the whole transaction, whatever the depth; without a transaction it is ignored
    private fun rollbackTransaction() {
        if (transactionDepth == 0) return
        transactionDepth = 0
        connection.rawExecSql("ROLLBACK")
    }
}

internal class SqliterPreparedStatement(
    private val statement: Statement,
    private val onFinalized: (SqliterPreparedStatement) -> Unit
) : PreparedStatement {
    private var cursor: Cursor? = null
    private var exhausted = false

    override fun step(): StepResult {
        if (exhausted) return StepResult.DONE
        val current = cursor ?: statement.query().also { cursor = it }
        val hasRow = current.next()
        if (current.columnCount == 0) {
            // A statement that returns no columns has just been executed; the next step() runs it again
            cursor = null
            statement.resetStatement()
            return StepResult.DONE
        }
        if (!hasRow) exhausted = true
        return if (hasRow) StepResult.ROW else StepResult.DONE
    }

    private fun row(): Cursor = checkNotNull(cursor) { "step() must return ROW before reading a column" }

    override fun getInt(index: Int): Int = row().getLong(index).toInt()
    override fun getLong(index: Int): Long = row().getLong(index)
    override fun getReal(index: Int): Double = row().getDouble(index)
    override fun getText(index: Int): String = row().getString(index)

    override fun getIntOrNull(index: Int): Int? = if (row().isNull(index)) null else getInt(index)
    override fun getLongOrNull(index: Int): Long? = if (row().isNull(index)) null else getLong(index)
    override fun getRealOrNull(index: Int): Double? = if (row().isNull(index)) null else getReal(index)
    override fun getTextOrNull(index: Int): String? = if (row().isNull(index)) null else getText(index)

    override fun bindInt(index: Int, value: Int) = statement.bindLong(index, value.toLong())
    override fun bindLong(index: Int, value: Long) = statement.bindLong(index, value)
    override fun bindReal(index: Int, value: Double) = statement.bindDouble(index, value)
    override fun bindText(index: Int, value: String) = statement.bindString(index, value)
    override fun bindNull(index: Int) = statement.bindNull(index)

    override fun reset() {
        cursor = null
        exhausted = false
        statement.resetStatement()
        statement.clearBindings()
    }

    override fun finalize() {
        cursor = null
        onFinalized(this)
        statement.finalizeStatement()
    }
}

/** `BEGIN`, `COMMIT` and `ROLLBACK` are not prepared in sqlite: they only update the transaction depth. */
private class TransactionStatement(private val action: () -> Unit) : PreparedStatement {
    override fun step(): StepResult {
        action()
        return StepResult.DONE
    }

    override fun getInt(index: Int) = throw UnsupportedOperationException()
    override fun getLong(index: Int) = throw UnsupportedOperationException()
    override fun getReal(index: Int) = throw UnsupportedOperationException()
    override fun getText(index: Int) = throw UnsupportedOperationException()
    override fun getIntOrNull(index: Int) = throw UnsupportedOperationException()
    override fun getLongOrNull(index: Int) = throw UnsupportedOperationException()
    override fun getRealOrNull(index: Int) = throw UnsupportedOperationException()
    override fun getTextOrNull(index: Int) = throw UnsupportedOperationException()
    override fun bindInt(index: Int, value: Int) = throw UnsupportedOperationException()
    override fun bindLong(index: Int, value: Long) = throw UnsupportedOperationException()
    override fun bindReal(index: Int, value: Double) = throw UnsupportedOperationException()
    override fun bindText(index: Int, value: String) = throw UnsupportedOperationException()
    override fun bindNull(index: Int) = throw UnsupportedOperationException()
    override fun reset() {}
    override fun finalize() {}
}

class SqliterDatabaseOpener : DatabaseOpener {
    /**
     * Opens [path], or a new private in-memory database when it is `:memory:`.
     *
     * sqliter manages `user_version`: it creates a new file with the configured version and rewrites the
     * version of a file that is older than it. To keep the semantics of the other platforms (a new file is
     * version 0 and opening never changes the version), the file's own version is read first and restored
     * if sqliter changed it.
     */
    override fun open(path: String): Database {
        val inMemory = path == ":memory:"
        val slash = path.lastIndexOf('/')
        // sqliter shares in-memory databases by name within the process, so every open needs its own name
        val name = if (inMemory) "memory-" + NSUUID().UUIDString else path.substring(slash + 1)
        val directory = if (inMemory || slash < 0) null else path.substring(0, slash)
        val peeked = if (inMemory) null else peekUserVersion(path)
        val configuration = DatabaseConfiguration(
            name = name,
            version = maxOf(peeked ?: 1, 1),
            create = { },
            upgrade = { _, _, _ -> },
            inMemory = inMemory,
            journalMode = JournalMode.DELETE,
            extendedConfig = DatabaseConfiguration.Extended(basePath = directory)
        )
        val connection = createDatabaseManager(configuration).createMultiThreadedConnection()
        val expected = peeked ?: 0
        if (connection.getVersion() != expected) connection.rawExecSql("PRAGMA user_version = $expected")
        return SqliterDatabase(connection)
    }
}
