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

import kotlinx.coroutines.test.runTest
import io.github.crisacm.salem.platform.io.SqliterDatabaseOpener
import io.github.crisacm.salem.platform.io.TestDatabaseHelper
import io.github.crisacm.salem.platform.io.copyAssetToTemp
import io.github.crisacm.salem.platform.io.getVersion
import io.github.crisacm.salem.platform.io.migrateTo
import io.github.crisacm.salem.core.DATABASE_VERSION
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Opens a copy of the database exported by Loop Habit Tracker (schema version 12), migrates it to the
 * current version with the real migrations, and reads it through the repositories.
 */
class LoopDbMigrationTest {
    // Number of repetitions per habit id in assets/test/loop.db
    private val repetitionsPerHabit = mapOf(
        15L to 33, 16L to 21, 17L to 2, 21L to 15, 22L to 12, 23L to 13, 24L to 43, 25L to 12, 26L to 13
    )

    @Test
    fun testLoopDatabaseMigratesFromVersion12() = runTest {
        val path = copyAssetToTemp("test/loop.db", "loop_migration.db")
        val db = SqliterDatabaseOpener().open(path)
        assertEquals(12, db.getVersion())

        db.migrateTo(DATABASE_VERSION) { version -> TestDatabaseHelper.loadMigrationSQL(version) }
        assertEquals(DATABASE_VERSION, db.getVersion())

        val habits = HabitRepository(db).findAll()
        assertEquals(9, habits.size)
        assertEquals(repetitionsPerHabit.keys, habits.map { it.id }.toSet())
        val first = habits.first()
        assertEquals("Wake up early", first.name)
        assertEquals(3, first.freqNum)
        assertEquals(7, first.freqDen)
        assertTrue(habits.all { !it.uuid.isNullOrEmpty() }, "migration 24 gives every habit an uuid")

        val entries = EntryRepository(db)
        for ((habitId, count) in repetitionsPerHabit) {
            assertEquals(count, entries.findAllByHabitId(habitId).size, "repetitions of habit $habitId")
        }
        assertEquals(164, repetitionsPerHabit.values.sum())

        val habit15 = entries.findAllByHabitId(15)
        val timestamps = habit15.map { it.timestamp }.toSet()
        assertTrue(1457913600000L in timestamps, "2016-03-14 is checked")
        assertTrue(1458086400000L in timestamps, "2016-03-16 is checked")
        assertFalse(1458172800000L in timestamps, "2016-03-17 is not checked")
        assertTrue(habit15.all { it.value == 2 }, "migration 13 stores every repetition as a manual check")
        db.close()
    }
}
