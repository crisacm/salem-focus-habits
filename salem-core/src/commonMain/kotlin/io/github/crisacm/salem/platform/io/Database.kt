package io.github.crisacm.salem.platform.io

import io.github.crisacm.salem.core.database.SQLParser

enum class StepResult { ROW, DONE }

interface PreparedStatement {
    fun step(): StepResult
    fun getInt(index: Int): Int
    fun getLong(index: Int): Long
    fun getReal(index: Int): Double
    fun getText(index: Int): String
    fun getIntOrNull(index: Int): Int?
    fun getLongOrNull(index: Int): Long?
    fun getRealOrNull(index: Int): Double?
    fun getTextOrNull(index: Int): String?
    fun bindInt(index: Int, value: Int)
    fun bindLong(index: Int, value: Long)
    fun bindReal(index: Int, value: Double)
    fun bindText(index: Int, value: String)
    fun bindNull(index: Int)
    fun reset()
    fun finalize()
}

interface Database {
    fun prepareStatement(sql: String): PreparedStatement
    fun close()
}

interface DatabaseOpener {
    fun open(path: String): Database
}

fun Database.run(sql: String) {
    val stmt = prepareStatement(sql)
    stmt.step()
    stmt.finalize()
}

fun Database.run(sql: String, bind: PreparedStatement.() -> Unit) {
    val stmt = prepareStatement(sql)
    stmt.bind()
    stmt.step()
    stmt.finalize()
}

fun Database.queryInt(sql: String): Int {
    val stmt = prepareStatement(sql)
    stmt.step()
    val value = stmt.getInt(0)
    stmt.finalize()
    return value
}

fun Database.queryLong(sql: String): Long {
    val stmt = prepareStatement(sql)
    stmt.step()
    val value = stmt.getLong(0)
    stmt.finalize()
    return value
}

fun Database.getVersion(): Int {
    return queryInt("PRAGMA user_version")
}

fun Database.setVersion(v: Int) {
    run("PRAGMA user_version = $v")
}

fun Database.begin() {
    run("BEGIN")
}

fun Database.commit() {
    run("COMMIT")
}

inline fun Database.query(
    sql: String,
    vararg params: String,
    block: (PreparedStatement) -> Unit
) {
    val stmt = prepareStatement(sql)
    for (i in params.indices) {
        stmt.bindText(i + 1, params[i])
    }
    while (stmt.step() == StepResult.ROW) {
        block(stmt)
    }
    stmt.finalize()
}

inline fun <T> Database.querySingle(
    sql: String,
    vararg params: String,
    block: (PreparedStatement) -> T
): T? {
    val stmt = prepareStatement(sql)
    for (i in params.indices) {
        stmt.bindText(i + 1, params[i])
    }
    val result = if (stmt.step() == StepResult.ROW) block(stmt) else null
    stmt.finalize()
    return result
}

/**
 * Applies the migrations after the current version up to [targetVersion]. With [atomic] each migration and its new
 * version number are one transaction: if one fails nothing of it stays and the version is the one before. Leave it off
 * when the caller already runs this inside a transaction, as Android does.
 */
suspend fun Database.migrateTo(
    targetVersion: Int,
    atomic: Boolean = false,
    loadMigrationSQL: suspend (Int) -> String
) {
    val currentVersion = getVersion()
    if (currentVersion >= targetVersion) return
    for (v in (currentVersion + 1)..targetVersion) {
        val commands = SQLParser.parse(loadMigrationSQL(v))
        if (atomic) run("BEGIN")
        try {
            for (cmd in commands) run(cmd)
            setVersion(v)
            if (atomic) run("COMMIT")
        } catch (e: Throwable) {
            if (atomic) run("ROLLBACK")
            throw e
        }
    }
}
