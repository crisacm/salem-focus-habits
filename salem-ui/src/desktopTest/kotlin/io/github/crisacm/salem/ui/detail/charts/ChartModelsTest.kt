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

package io.github.crisacm.salem.ui.detail.charts

import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.Score
import io.github.crisacm.salem.core.ui.views.HistoryChart.Square
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChartModelsTest {
    // 2015-01-25 is a Sunday
    private val sunday = LocalDate(2015, 1, 25)

    @Test
    fun theScoreIsKeptBetweenZeroAndOne() {
        val points = scorePoints(listOf(Score(sunday, 0.73), Score(sunday.minus(7), 1.4), Score(sunday.minus(14), -0.2)))
        assertEquals(listOf(0.73f, 1f, 0f), points.map { it.value })
        assertEquals(sunday, points.first().date)
    }

    @Test
    fun aScoreIsShownAsAPercentage() {
        assertEquals("73%", percentText(0.73f))
        assertEquals("72%", percentText(0.7249f))
        assertEquals("100%", percentText(1f))
    }

    @Test
    fun theBarsUseThousandthsAndTheLargestFillsTheChart() {
        val bars = barColumns(listOf(Entry(sunday, 20000), Entry(sunday.minus(30), 12500)))
        assertEquals(listOf(20.0, 12.5), bars.map { it.value })
        assertEquals(20.0, barScaleMax(bars.map { it.value }))
        assertEquals(1.0, barScaleMax(emptyList()))
        assertEquals(1.0, barScaleMax(listOf(0.0, 0.5)))
    }

    @Test
    fun aBarNumberIsWholeOrShort() {
        assertEquals("20", barValueText(20.0))
        assertEquals("12.5", barValueText(12.5))
        assertEquals("0", barValueText(0.0))
    }

    @Test
    fun theLabelUnderABarFollowsItsBucket() {
        val name = { _: LocalDate -> "March" }
        val date = LocalDate(2021, 3, 8)
        assertEquals("8", barAxisLabel(1, date, name))
        assertEquals("8", barAxisLabel(7, date, name))
        assertEquals("M", barAxisLabel(31, date, name))
        assertEquals("Q1", barAxisLabel(92, date, name))
        assertEquals("Q3", barAxisLabel(92, LocalDate(2021, 8, 1), name))
        assertEquals("2021", barAxisLabel(365, date, name))
    }

    @Test
    fun theDaysOfTheWeekAreAddedUpAcrossTheMonths() {
        // Saturday first: Sat, Sun, Mon, Tue, Wed, Thu, Fri
        val frequency = hashMapOf(
            LocalDate(2015, 1, 1) to arrayOf(1, 2, 3, 4, 5, 6, 7),
            LocalDate(2015, 2, 1) to arrayOf(10, 20, 30, 40, 50, 60, 70)
        )
        val bars = weekdayBars(frequency, isNumerical = false, firstWeekday = DayOfWeek.MONDAY)
        assertEquals(
            listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            ),
            bars.map { it.day }
        )
        assertEquals(listOf(33, 44, 55, 66, 77, 11, 22), bars.map { it.total })
    }

    @Test
    fun aNumericalHabitCountsThousandths() {
        val bars = weekdayBars(hashMapOf(LocalDate(2015, 1, 1) to arrayOf(2500, 0, 0, 0, 0, 0, 0)), true, DayOfWeek.SATURDAY)
        assertEquals(3, bars.first().total)
    }

    @Test
    fun theBusiestDayIsHighlighted() {
        val bars = weekdayBars(hashMapOf(LocalDate(2015, 1, 1) to arrayOf(5, 0, 9, 9, 1, 0, 0)), false, DayOfWeek.SATURDAY)
        assertEquals(2, highlightedWeekday(bars))
        assertEquals(-1, highlightedWeekday(weekdayBars(hashMapOf(), false, DayOfWeek.SATURDAY)))
    }

    @Test
    fun aBarFillsAPartOfTheWidth() {
        assertEquals(0.5f, barFraction(10, 20))
        assertEquals(0f, barFraction(5, 0))
        assertEquals(1f, barFraction(30, 20))
    }

    @Test
    fun theCalendarHasAColumnPerWeekWithTheNewestFirst() {
        // Three days of data ending on Sunday the 25th, weeks that start on Monday: the week of the 19th and the one before it
        val series = listOf(Square.ON, Square.OFF, Square.HATCHED)
        val columns = calendarColumns(series, Square.OFF, sunday, DayOfWeek.MONDAY)
        assertEquals(1, columns.size)
        assertEquals(LocalDate(2015, 1, 19), columns[0].weekStart)
        // Mon 19 .. Sun 25: the 25th is done, the 24th missed and the 23rd skipped
        assertEquals(DotState.DONE, columns[0].dots[6]?.state)
        assertEquals(DotState.MISSED, columns[0].dots[5]?.state)
        assertEquals(DotState.SKIPPED, columns[0].dots[4]?.state)
        // Days before the series use the default one
        assertEquals(DotState.MISSED, columns[0].dots[0]?.state)
    }

    @Test
    fun aDayThatHasNotComeYetHasNoDot() {
        val thursday = LocalDate(2015, 1, 22)
        val columns = calendarColumns(listOf(Square.ON), Square.OFF, thursday, DayOfWeek.MONDAY)
        assertEquals(DotState.DONE, columns[0].dots[3]?.state)
        assertNull(columns[0].dots[4])
        assertNull(columns[0].dots[6])
    }

    @Test
    fun theCalendarGoesBackToTheWeekOfTheOldestDay() {
        val series = List(14) { Square.ON }
        val columns = calendarColumns(series, Square.OFF, sunday, DayOfWeek.MONDAY)
        // The oldest day is 13 days before the 25th, Monday the 12th, so there are two weeks
        assertEquals(listOf(LocalDate(2015, 1, 19), LocalDate(2015, 1, 12)), columns.map { it.weekStart })
    }

    @Test
    fun aMonthIsNamedOverTheWeekThatHasItsFirstDay() {
        // Weeks starting on Monday: the 1st of February 2015 is a Sunday, in the week that starts on Jan 26
        val series = List(40) { Square.ON }
        val columns = calendarColumns(series, Square.OFF, LocalDate(2015, 2, 10), DayOfWeek.MONDAY)
        val labels = monthLabels(columns)
        assertEquals(listOf(LocalDate(2015, 2, 1)), labels.map { it.date }.filter { it.month == 2 })
        assertEquals(LocalDate(2015, 1, 26), columns[labels.first { it.date.month == 2 }.column].weekStart)
    }

    @Test
    fun theLettersFollowTheFirstWeekday() {
        val letters = weekdayLetters(DayOfWeek.MONDAY) { it.name.take(1) }
        assertEquals(listOf("M", "T", "W", "T", "F", "S", "S"), letters)
    }
}
