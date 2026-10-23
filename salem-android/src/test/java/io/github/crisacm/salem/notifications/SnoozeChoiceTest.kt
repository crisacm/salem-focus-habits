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

package io.github.crisacm.salem.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class SnoozeChoiceTest {
    @Test
    fun nonNegativeValuesAreDelaysInMinutes() {
        assertEquals(SnoozeChoice.Delay(15), snoozeChoice(15))
        assertEquals(SnoozeChoice.Delay(1440), snoozeChoice(1440))
        assertEquals(SnoozeChoice.Delay(0), snoozeChoice(0))
    }

    @Test
    fun negativeValueMeansCustomTime() {
        assertEquals(SnoozeChoice.CustomTime, snoozeChoice(-1))
    }
}
