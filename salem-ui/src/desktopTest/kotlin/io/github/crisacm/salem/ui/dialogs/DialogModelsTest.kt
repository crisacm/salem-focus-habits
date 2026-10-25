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

package io.github.crisacm.salem.ui.dialogs

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DialogModelsTest {
    @Test
    fun toggleSetsOnlyTheChosenDay() {
        val days = booleanArrayOf(false, true, false)
        val result = toggleDay(days, 2, true)
        assertArrayEquals(booleanArrayOf(false, true, true), result)
        assertArrayEquals(booleanArrayOf(false, true, false), days)
    }

    @Test
    fun toggleCanClearADay() {
        assertArrayEquals(booleanArrayOf(false, false), toggleDay(booleanArrayOf(true, false), 0, false))
    }

    @Test
    fun swatchesKeepTheSerpentineLayoutOfTheOriginalPicker() {
        val rows = paletteRows(count = 20, columns = 4)
        assertEquals(5, rows.size)
        assertEquals(listOf(0, 1, 2, 3), rows[0].map { it.index })
        assertEquals(listOf(7, 6, 5, 4), rows[1].map { it.index })
        assertEquals(listOf(8, 9, 10, 11), rows[2].map { it.index })
    }

    @Test
    fun swatchNumbersFollowTheReadingOrder() {
        val rows = paletteRows(count = 20, columns = 4)
        assertEquals(listOf(1, 2, 3, 4), rows[0].map { it.number })
        assertEquals(listOf(5, 6, 7, 8), rows[1].map { it.number })
        assertEquals(20, rows.last().last().number)
    }

    @Test
    fun everyColorAppearsOnce() {
        val all = paletteRows(count = 18, columns = 4).flatten().map { it.index }.sorted()
        assertEquals((0 until 18).toList(), all)
    }
}
