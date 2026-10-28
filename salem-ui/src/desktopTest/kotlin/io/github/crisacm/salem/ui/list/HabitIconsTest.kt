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

package io.github.crisacm.salem.ui.list

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitIconsTest {
    @Test
    fun theSameKeyAlwaysGivesTheSameIcon() {
        assertEquals(habitIconIndex("a1b2c3", 28), habitIconIndex("a1b2c3", 28))
        assertEquals(habitIconFor("a1b2c3", null), habitIconFor("a1b2c3", 7L))
    }

    @Test
    fun theIndexIsAlwaysInsideThePool() {
        val keys = listOf("", "0", "zzzz", "ffffffff-ffff-ffff-ffff-ffffffffffff") + (1..200).map { "habit-$it" }
        for (size in listOf(1, 7, 28)) {
            for (key in keys) assertTrue(habitIconIndex(key, size) in 0 until size)
        }
    }

    @Test
    fun aHabitWithoutUuidFallsBackToItsId() {
        assertEquals(habitIconFor(null, 12L), habitIconFor(null, 12L))
    }

    @Test
    fun manyHabitsUseManyDifferentIcons() {
        val used = (1..100).map { habitIconIndex("uuid-$it", HabitIconPool.size) }.toSet()
        assertTrue(used.size > HabitIconPool.size / 2)
    }
}
