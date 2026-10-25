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

package io.github.crisacm.salem.core.database

import io.github.crisacm.salem.core.DATABASE_VERSION
import io.github.crisacm.salem.platform.io.TestDatabaseHelper
import io.github.crisacm.salem.platform.io.createTestDatabaseOpener
import io.github.crisacm.salem.platform.io.getVersion
import io.github.crisacm.salem.platform.io.query
import io.github.crisacm.salem.platform.io.setVersion
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HabitsSchemaTest {
    private val load: suspend (Int) -> String = { TestDatabaseHelper.loadMigrationSQL(it) }

    private fun tables(db: io.github.crisacm.salem.platform.io.Database): List<String> {
        val names = mutableListOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type = 'table'") { names += it.getText(0) }
        return names
    }

    @Test
    fun aNewFileGetsTheWholeSchema() = runTest {
        val db = createTestDatabaseOpener().open(":memory:")
        db.initializeHabitsSchema(loadMigrationSQL = load)
        assertEquals(DATABASE_VERSION, db.getVersion())
        assertTrue("Habits" in tables(db))
        assertTrue("Repetitions" in tables(db))
    }

    @Test
    fun anOlderSupportedFileIsMigrated() = runTest {
        val db = createTestDatabaseOpener().open(":memory:")
        db.setVersion(8)
        db.initializeHabitsSchema(loadMigrationSQL = load)
        assertEquals(DATABASE_VERSION, db.getVersion())
    }

    @Test
    fun aFileThatIsAlreadyCurrentIsLeftAlone() = runTest {
        val db = createTestDatabaseOpener().open(":memory:")
        db.initializeHabitsSchema(loadMigrationSQL = load)
        db.initializeHabitsSchema(loadMigrationSQL = load)
        assertEquals(DATABASE_VERSION, db.getVersion())
    }

    @Test
    fun aTooOldFileIsRefused() = runTest {
        val db = createTestDatabaseOpener().open(":memory:")
        db.setVersion(5)
        assertFailsWith<UnsupportedDatabaseVersionException> { db.initializeHabitsSchema(loadMigrationSQL = load) }
    }

    @Test
    fun aNewerFileIsRefused() = runTest {
        val db = createTestDatabaseOpener().open(":memory:")
        db.setVersion(DATABASE_VERSION + 1)
        assertFailsWith<UnsupportedDatabaseVersionException> { db.initializeHabitsSchema(loadMigrationSQL = load) }
    }

    @Test
    fun aMigrationThatFailsHalfWayLeavesNothingBehind() = runTest {
        val db = createTestDatabaseOpener().open(":memory:")
        db.setVersion(11)
        val broken: suspend (Int) -> String = { version ->
            if (version == 12) "CREATE TABLE half_done (id INTEGER); THIS IS NOT SQL;" else TestDatabaseHelper.loadMigrationSQL(version)
        }
        assertFails { db.initializeHabitsSchema(loadMigrationSQL = broken) }
        // The first statement of the migration was rolled back, and so was the version
        assertEquals(11, db.getVersion())
        assertFalse("half_done" in tables(db))
    }
}
