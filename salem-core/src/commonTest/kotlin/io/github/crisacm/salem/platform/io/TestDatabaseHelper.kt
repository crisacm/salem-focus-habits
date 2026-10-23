package io.github.crisacm.salem.platform.io

expect object TestDatabaseHelper {
    suspend fun createEmptyDatabase(): Database
    suspend fun loadMigrationSQL(version: Int): String
}
