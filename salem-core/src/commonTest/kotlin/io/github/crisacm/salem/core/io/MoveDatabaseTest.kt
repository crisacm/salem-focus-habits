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
import io.github.crisacm.salem.platform.io.UserFile
import io.github.crisacm.salem.platform.io.getVersion
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoveDatabaseTest : BaseUnitTest() {
    private suspend fun databaseFile(name: String): UserFile = createTempDir().resolve(name)

    private suspend fun realDatabase(): UserFile = copyResourceToTempFile("loop.db")

    @Test
    fun movesTheDatabaseToTheNewPlace() = runTest {
        val old = realDatabase()
        val bytes = old.readBytes(1_000_000)
        val new = databaseFile("uhabits.db")

        assertTrue(moveDatabase(old, new))

        assertContentEquals(bytes, new.readBytes(1_000_000))
        assertFalse(old.exists())
        assertEquals(12, databaseOpener().open(new.pathString).getVersion())
    }

    @Test
    fun aDatabaseThatIsAlreadyInTheNewPlaceIsNotReplaced() = runTest {
        val old = realDatabase()
        val new = databaseFile("uhabits.db")
        new.writeString("SQLite format 3\u0000 the one in use")

        assertFalse(moveDatabase(old, new))

        assertEquals(listOf("SQLite format 3\u0000 the one in use"), new.lines())
        assertTrue(old.exists())
    }

    @Test
    fun nothingToMoveWhenThereIsNoOldDatabase() = runTest {
        val old = databaseFile("old.db")
        val new = databaseFile("uhabits.db")

        assertFalse(moveDatabase(old, new))

        assertFalse(new.exists())
    }

    @Test
    fun theOldDatabaseStaysIfTheCopyIsNotADatabase() = runTest {
        val old = databaseFile("old.db")
        old.writeBytes(ByteArray(0))
        val new = databaseFile("uhabits.db")

        assertFalse(moveDatabase(old, new))

        assertTrue(old.exists())
        assertFalse(new.exists())
    }
}
