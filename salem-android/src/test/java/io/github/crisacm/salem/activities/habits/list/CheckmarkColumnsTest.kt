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

package io.github.crisacm.salem.activities.habits.list

import org.junit.Assert.assertEquals
import org.junit.Test

class CheckmarkColumnsTest {
    // A phone in portrait: the name takes a third of the width or the minimum name width, whichever is larger.
    @Test
    fun theNameKeepsAtLeastItsMinimumWidth() {
        assertEquals(4, checkmarkColumns(width = 1080f, nameWidth = 480f, buttonWidth = 150f))
        assertEquals(2, checkmarkColumns(width = 900f, nameWidth = 480f, buttonWidth = 150f))
    }

    @Test
    fun theNameTakesAThirdOfAWideScreen() {
        assertEquals(10, checkmarkColumns(width = 2400f, nameWidth = 480f, buttonWidth = 150f))
    }

    @Test
    fun neverNegativeAndNeverMoreThanTheMaximum() {
        assertEquals(0, checkmarkColumns(width = 100f, nameWidth = 480f, buttonWidth = 150f))
        assertEquals(0, checkmarkColumns(width = 0f, nameWidth = 480f, buttonWidth = 150f))
        assertEquals(MAX_CHECKMARK_COUNT, checkmarkColumns(width = 100000f, nameWidth = 480f, buttonWidth = 150f))
    }
}
