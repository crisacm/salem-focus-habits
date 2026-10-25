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

import io.github.crisacm.salem.core.models.WeekdayList
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditHelpersTest {
    // Saturday first, as a WeekdayList.
    private val short = listOf("Sa", "Su", "Mo", "Tu", "We", "Th", "Fr")
    private val long = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    private fun days(vararg index: Int) = WeekdayList(BooleanArray(7) { it in index })

    @Test
    fun oneDayShowsItsLongName() {
        assertEquals(WeekdaysSummary.Single("Monday"), weekdaysSummary(days(2), short, long))
    }

    @Test
    fun saturdayAndSundayAreTheWeekend() {
        assertEquals(WeekdaysSummary.Weekends, weekdaysSummary(days(0, 1), short, long))
    }

    @Test
    fun mondayToFridayAreAnyWeekday() {
        assertEquals(WeekdaysSummary.AnyWeekday, weekdaysSummary(days(2, 3, 4, 5, 6), short, long))
    }

    @Test
    fun allSevenDaysAreAnyDay() {
        assertEquals(WeekdaysSummary.AnyDay, weekdaysSummary(days(0, 1, 2, 3, 4, 5, 6), short, long))
    }

    @Test
    fun otherSelectionsListTheShortNamesInOrder() {
        assertEquals(WeekdaysSummary.Custom("Sa, Mo, Fr"), weekdaysSummary(days(0, 2, 6), short, long))
        assertEquals(WeekdaysSummary.Custom("Su, Mo"), weekdaysSummary(days(1, 2), short, long))
    }

    @Test
    fun noDaysGiveAnEmptyText() {
        assertEquals(WeekdaysSummary.Custom(""), weekdaysSummary(days(), short, long))
    }

    @Test
    fun theNumericalFrequencyChoicesMapToTheDenominator() {
        assertEquals(1, denominatorForChoice(0))
        assertEquals(7, denominatorForChoice(1))
        assertEquals(30, denominatorForChoice(2))
        assertEquals(1, denominatorForChoice(9))
    }

    @Test
    fun theNumericalFrequencyKindFollowsTheDenominator() {
        assertEquals(NumericalFrequencyKind.EVERY_DAY, numericalFrequencyKind(1))
        assertEquals(NumericalFrequencyKind.EVERY_WEEK, numericalFrequencyKind(7))
        assertEquals(NumericalFrequencyKind.EVERY_MONTH, numericalFrequencyKind(30))
        assertEquals(NumericalFrequencyKind.OTHER, numericalFrequencyKind(14))
    }

    @Test
    fun theReminderStartsAtEightWhenUnset() {
        assertEquals(8 to 0, reminderStartTime(-1, -1))
        assertEquals(21 to 15, reminderStartTime(21, 15))
        assertEquals(0 to 0, reminderStartTime(0, 0))
    }

    @Test
    fun aTwentyFourHourPatternHasCapitalH() {
        assertTrue(uses24Hour("HH:mm"))
        assertTrue(uses24Hour("H"))
        assertTrue(uses24Hour("kk 'Uhr'"))
    }

    @Test
    fun aTwelveHourPatternHasTheMarker() {
        assertFalse(uses24Hour("h:mm a"))
        assertFalse(uses24Hour("a h"))
    }

    @Test
    fun quotedLettersAreNotPatternLetters() {
        assertFalse(uses24Hour("'H' h a"))
        assertTrue(uses24Hour("H 'h'"))
    }
}
