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
import io.github.crisacm.salem.platform.io.run
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExternalChangesTest : BaseUnitTest() {
    private suspend fun twoConnections(): Pair<io.github.crisacm.salem.platform.io.Database, io.github.crisacm.salem.platform.io.Database> {
        val file = createTempDir().resolve("shared.db")
        val opener = databaseOpener()
        val first = opener.open(file.pathString)
        first.run("CREATE TABLE t (x INTEGER)")
        return first to opener.open(file.pathString)
    }

    @Test
    fun theFirstCheckSeesNothingNew() = runTest {
        val (mine, _) = twoConnections()
        assertFalse(ExternalChanges(mine).hasChanged())
    }

    @Test
    fun aChangeOfAnotherConnectionIsSeenOnce() = runTest {
        val (mine, other) = twoConnections()
        val changes = ExternalChanges(mine)

        other.run("INSERT INTO t VALUES (1)")

        assertTrue(changes.hasChanged())
        assertFalse(changes.hasChanged())
    }

    @Test
    fun myOwnChangesAreNotExternal() = runTest {
        val (mine, _) = twoConnections()
        val changes = ExternalChanges(mine)

        mine.run("INSERT INTO t VALUES (1)")

        assertFalse(changes.hasChanged())
    }
}
