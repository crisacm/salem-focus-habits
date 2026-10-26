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

package io.github.crisacm.salem.core.io

import io.github.crisacm.salem.core.BaseUnitTest
import io.github.crisacm.salem.core.DATABASE_VERSION
import io.github.crisacm.salem.core.models.sqlite.SQLModelFactory
import io.github.crisacm.salem.core.models.sqlite.SQLiteHabitList
import io.github.crisacm.salem.platform.io.Database
import io.github.crisacm.salem.platform.io.TestDatabaseHelper
import io.github.crisacm.salem.platform.io.UserFile
import io.github.crisacm.salem.platform.io.getVersion
import io.github.crisacm.salem.platform.io.migrateTo
import io.github.crisacm.salem.platform.io.setVersion
import io.github.crisacm.salem.platform.time.LocalDate
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseBackupTest : BaseUnitTest() {
    private lateinit var database: Database
    private lateinit var source: UserFile
    private lateinit var importer: GenericImporter
    private lateinit var runner: ImportRunner

    // A database in a file, as the apps have, with the 9 habits of loop.db imported
    private suspend fun databaseWithHabits() {
        source = createTempDir().resolve("app.db")
        database = databaseOpener().open(source.pathString)
        database.setVersion(8)
        database.migrateTo(DATABASE_VERSION) { TestDatabaseHelper.loadMigrationSQL(it) }
        val factory = SQLModelFactory(database)
        val list = SQLiteHabitList(factory)
        habitList = list
        importer = buildImporter(factory, list)
        runner = ImportRunner(importer, database) { list.reload() }
        assertEquals(ImportResult.SUCCESS, runner.run(copyResourceToTempFile("loop.db")))
        assertEquals(9, habitList.size())
    }

    private suspend fun buildImporter(factory: SQLModelFactory, list: SQLiteHabitList): GenericImporter {
        val opener = databaseOpener()
        return GenericImporter(
            LoopDBImporter(list, factory, opener, commandRunner, StandardLogging(), fileOpener),
            RewireDBImporter(list, factory, opener),
            TickmateDBImporter(list, factory, opener),
            HabitBullCSVImporter(list, factory, StandardLogging())
        )
    }

    private suspend fun backupFile(name: String = "backup.db"): UserFile = createTempDir().resolve(name)

    @Test
    fun theCopyHasTheSameVersionAndTheSameHabits() = runTest {
        databaseWithHabits()
        val target = backupFile()

        backupDatabase(source, target)

        assertTrue(target.exists())
        val copy = databaseOpener().open(target.pathString)
        assertEquals(DATABASE_VERSION, copy.getVersion())
        val copiedFactory = SQLModelFactory(copy)
        assertEquals(9, SQLiteHabitList(copiedFactory).size())
    }

    @Test
    fun anExistingFileIsReplaced() = runTest {
        databaseWithHabits()
        val target = backupFile()
        target.writeString("not a database")

        backupDatabase(source, target)

        assertEquals(DATABASE_VERSION, databaseOpener().open(target.pathString).getVersion())
    }

    @Test
    fun aNameWithAnApostropheWorks() = runTest {
        databaseWithHabits()
        val target = backupFile("Anna's backup.db")

        backupDatabase(source, target)

        assertEquals(DATABASE_VERSION, databaseOpener().open(target.pathString).getVersion())
    }

    @Test
    fun theBackupCanBeImportedAgain() = runTest {
        databaseWithHabits()
        val target = backupFile()
        backupDatabase(source, target)

        // A new app database imports the copy, as the user would after reinstalling
        val fresh = buildMemoryDatabase()
        val factory = SQLModelFactory(fresh)
        val list = SQLiteHabitList(factory)
        habitList = list
        val result = ImportRunner(buildImporter(factory, list), fresh) { list.reload() }.run(target)

        assertEquals(ImportResult.SUCCESS, result)
        assertEquals(9, habitList.size())
        assertEquals("Wake up early", habitList.getByPosition(0).name)
    }

    @Test
    fun theFileNameHasTheDate() {
        assertEquals("Salem Backup 2026-10-25.db", backupFileName(LocalDate(2026, 10, 25)))
    }
}
