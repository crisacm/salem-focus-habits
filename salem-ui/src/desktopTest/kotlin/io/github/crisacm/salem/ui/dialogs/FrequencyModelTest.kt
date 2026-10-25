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

import org.junit.Assert.assertEquals
import org.junit.Test

class FrequencyModelTest {
    private fun input(
        mode: FrequencyMode,
        everyXDays: String = "3",
        perWeek: String = "3",
        perMonth: String = "10",
        x: String = "3",
        y: String = "14"
    ) = FrequencyInput(mode, everyXDays, perWeek, perMonth, x, y)

    private fun pick(input: FrequencyInput) = frequencyFromInput(input)

    @Test
    fun everyDayIsOneOverOne() {
        assertEquals(1 to 1, pick(input(FrequencyMode.EVERY_DAY)))
    }

    @Test
    fun everyXDaysIsOneOverX() {
        assertEquals(1 to 3, pick(input(FrequencyMode.EVERY_X_DAYS, everyXDays = "3")))
        assertEquals(1 to 365, pick(input(FrequencyMode.EVERY_X_DAYS, everyXDays = "365")))
    }

    @Test
    fun timesPerWeekIsXOverSeven() {
        assertEquals(3 to 7, pick(input(FrequencyMode.X_TIMES_PER_WEEK, perWeek = "3")))
    }

    @Test
    fun timesPerMonthIsXOverThirty() {
        assertEquals(10 to 30, pick(input(FrequencyMode.X_TIMES_PER_MONTH, perMonth = "10")))
        assertEquals(1 to 30, pick(input(FrequencyMode.X_TIMES_PER_MONTH, perMonth = "1")))
    }

    @Test
    fun timesInYDaysUsesBothNumbers() {
        assertEquals(3 to 14, pick(input(FrequencyMode.X_TIMES_IN_Y_DAYS, x = "3", y = "14")))
    }

    @Test
    fun emptyFieldsFallBackToEveryDay() {
        assertEquals(1 to 1, pick(input(FrequencyMode.EVERY_X_DAYS, everyXDays = "")))
        assertEquals(1 to 1, pick(input(FrequencyMode.X_TIMES_PER_WEEK, perWeek = "")))
        assertEquals(1 to 1, pick(input(FrequencyMode.X_TIMES_PER_MONTH, perMonth = "")))
        assertEquals(1 to 1, pick(input(FrequencyMode.X_TIMES_IN_Y_DAYS, x = "", y = "14")))
        assertEquals(1 to 1, pick(input(FrequencyMode.X_TIMES_IN_Y_DAYS, x = "3", y = "")))
    }

    @Test
    fun invalidCombinationsAreResetToEveryDay() {
        assertEquals(1 to 1, pick(input(FrequencyMode.EVERY_X_DAYS, everyXDays = "1")))
        assertEquals(1 to 1, pick(input(FrequencyMode.EVERY_X_DAYS, everyXDays = "0")))
        assertEquals(1 to 1, pick(input(FrequencyMode.X_TIMES_PER_WEEK, perWeek = "7")))
        assertEquals(1 to 1, pick(input(FrequencyMode.X_TIMES_PER_WEEK, perWeek = "9")))
        assertEquals(1 to 1, pick(input(FrequencyMode.X_TIMES_PER_WEEK, perWeek = "0")))
        assertEquals(1 to 1, pick(input(FrequencyMode.X_TIMES_PER_MONTH, perMonth = "30")))
        assertEquals(1 to 1, pick(input(FrequencyMode.X_TIMES_IN_Y_DAYS, x = "5", y = "5")))
        assertEquals(1 to 1, pick(input(FrequencyMode.X_TIMES_IN_Y_DAYS, x = "6", y = "5")))
    }

    @Test
    fun numbersThatDoNotFitAreTreatedAsEmpty() {
        assertEquals(1 to 1, pick(input(FrequencyMode.EVERY_X_DAYS, everyXDays = "99999999999")))
    }

    @Test
    fun reopeningPicksTheModeOfTheStoredFrequency() {
        assertEquals(FrequencyMode.EVERY_DAY, initialFrequencyInput(1, 1).mode)
        assertEquals(FrequencyMode.EVERY_X_DAYS, initialFrequencyInput(1, 5).mode)
        assertEquals(FrequencyMode.EVERY_X_DAYS, initialFrequencyInput(1, 7).mode)
        assertEquals(FrequencyMode.X_TIMES_PER_WEEK, initialFrequencyInput(3, 7).mode)
        assertEquals(FrequencyMode.X_TIMES_PER_MONTH, initialFrequencyInput(10, 30).mode)
        assertEquals(FrequencyMode.X_TIMES_PER_MONTH, initialFrequencyInput(1, 30).mode)
        assertEquals(FrequencyMode.X_TIMES_PER_MONTH, initialFrequencyInput(2, 31).mode)
        assertEquals(FrequencyMode.X_TIMES_IN_Y_DAYS, initialFrequencyInput(3, 14).mode)
    }

    @Test
    fun reopeningFillsOnlyTheFieldsOfTheSelectedMode() {
        val everyX = initialFrequencyInput(1, 5)
        assertEquals("5", everyX.everyXDays)
        assertEquals("3", everyX.perWeek)
        assertEquals("10", everyX.perMonth)
        val week = initialFrequencyInput(2, 7)
        assertEquals("2", week.perWeek)
        assertEquals("3", week.everyXDays)
        val month = initialFrequencyInput(4, 30)
        assertEquals("4", month.perMonth)
        val inDays = initialFrequencyInput(2, 9)
        assertEquals("2", inDays.x)
        assertEquals("9", inDays.y)
    }

    @Test
    fun savingWithoutChangesKeepsTheFrequency() {
        listOf(1 to 1, 1 to 5, 3 to 7, 10 to 30, 3 to 14).forEach { (num, den) ->
            assertEquals(num to den, frequencyFromInput(initialFrequencyInput(num, den)))
        }
    }

    @Test
    fun oneOverThirtyIsReopenedAsOncePerMonth() {
        assertEquals(1 to 30, frequencyFromInput(initialFrequencyInput(1, 30)))
    }

    @Test
    fun labelsFollowTheOriginalPriority() {
        assertEquals(FrequencyLabel.EveryMonth, frequencyLabel(1, 30))
        assertEquals(FrequencyLabel.EveryMonth, frequencyLabel(1, 31))
        assertEquals(FrequencyLabel.TimesPerMonth(3), frequencyLabel(3, 30))
        assertEquals(FrequencyLabel.EveryDay, frequencyLabel(1, 1))
        assertEquals(FrequencyLabel.EveryWeek, frequencyLabel(1, 7))
        assertEquals(FrequencyLabel.EveryXDays(5), frequencyLabel(1, 5))
        assertEquals(FrequencyLabel.TimesPerWeek(3), frequencyLabel(3, 7))
        assertEquals(FrequencyLabel.TimesInDays(2, 9), frequencyLabel(2, 9))
    }
}
