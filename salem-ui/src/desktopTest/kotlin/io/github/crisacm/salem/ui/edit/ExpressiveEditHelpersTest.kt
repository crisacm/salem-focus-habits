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

package io.github.crisacm.salem.ui.edit

import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.platform.time.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpressiveEditHelpersTest {
    @Test
    fun aQuickColourKeepsTheQuickSwatches() {
        assertEquals(QuickColors, swatchColors(PaletteColor(15)))
    }

    @Test
    fun anotherColourTakesTheFirstSwatch() {
        assertEquals(listOf(8, 15, 2, 13), swatchColors(PaletteColor(8)))
    }

    @Test
    fun theChipsTellTheFrequencyOfAYesNoHabit() {
        assertEquals(FrequencyChoice.EVERY_DAY, frequencyChoice(1, 1, numerical = false))
        assertEquals(FrequencyChoice.PER_WEEK, frequencyChoice(1, 7, numerical = false))
        assertEquals(FrequencyChoice.CUSTOM, frequencyChoice(3, 7, numerical = false))
        assertEquals(FrequencyChoice.CUSTOM, frequencyChoice(2, 1, numerical = false))
    }

    @Test
    fun aNumericalHabitIsToldByItsPeriodAlone() {
        assertEquals(FrequencyChoice.EVERY_DAY, frequencyChoice(1, 1, numerical = true))
        assertEquals(FrequencyChoice.PER_WEEK, frequencyChoice(3, 7, numerical = true))
        assertEquals(FrequencyChoice.CUSTOM, frequencyChoice(1, 30, numerical = true))
    }

    @Test
    fun theWeekStartsAtTheFirstWeekdayAndKeepsTheListPositions() {
        val slots = weekdaySlots(DayOfWeek.MONDAY)
        assertEquals(
            listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            ),
            slots.map { it.day }
        )
        // A WeekdayList starts at Saturday
        assertEquals(listOf(2, 3, 4, 5, 6, 0, 1), slots.map { it.index })
    }

    @Test
    fun aDayIsToggledAndTheLastOneStays() {
        val monday = weekdaySlots(DayOfWeek.MONDAY).first().index
        val withoutMonday = toggleWeekday(WeekdayList.EVERY_DAY, monday)
        assertTrue(!withoutMonday.toArray()[monday])
        assertTrue(toggleWeekday(withoutMonday, monday).toArray()[monday])
        val onlyMonday = WeekdayList(BooleanArray(7) { it == monday })
        assertEquals(onlyMonday, toggleWeekday(onlyMonday, monday))
    }
}
