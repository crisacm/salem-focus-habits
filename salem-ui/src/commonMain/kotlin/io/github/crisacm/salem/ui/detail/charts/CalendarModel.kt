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

import io.github.crisacm.salem.core.ui.views.HistoryChart
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.getWeekdaySequence

/** How a day looks in the calendar. */
enum class DotState { DONE, SOFT, SKIPPED, MISSED }

class CalendarDot(val date: LocalDate, val state: DotState)

/** A week of the calendar: its first day and a dot per day in the order of the week, or null for a day that has not come yet. */
class CalendarColumn(val weekStart: LocalDate, val dots: List<CalendarDot?>)

/** A month name over the week that has its first day. */
class MonthLabel(val column: Int, val date: LocalDate)

private fun dotState(square: HistoryChart.Square) = when (square) {
    HistoryChart.Square.ON -> DotState.DONE
    HistoryChart.Square.DIMMED, HistoryChart.Square.GREY -> DotState.SOFT
    HistoryChart.Square.HATCHED -> DotState.SKIPPED
    HistoryChart.Square.OFF -> DotState.MISSED
}

/**
 * The weeks of the calendar, newest first, up to the week of the oldest day of [series] (which has the day of
 * today first, then the day before...). A day that is not in the series gets [defaultSquare].
 */
fun calendarColumns(
    series: List<HistoryChart.Square>,
    defaultSquare: HistoryChart.Square,
    today: LocalDate,
    firstWeekday: DayOfWeek
): List<CalendarColumn> {
    val oldest = today.minus(maxOf(series.size - 1, 0))
    val newestWeek = today.startOfWeek(firstWeekday)
    val oldestWeek = oldest.startOfWeek(firstWeekday)
    val count = oldestWeek.daysUntil(newestWeek) / 7 + 1
    return List(count) { column ->
        val weekStart = newestWeek.minus(7 * column)
        CalendarColumn(
            weekStart,
            List(7) { row ->
                val date = weekStart.plus(row)
                if (date.isNewerThan(today)) {
                    null
                } else {
                    CalendarDot(date, dotState(series.getOrElse(date.daysUntil(today)) { defaultSquare }))
                }
            }
        )
    }
}

/** The month names to draw: one over each week that contains the first day of a month. */
fun monthLabels(columns: List<CalendarColumn>): List<MonthLabel> = columns.mapIndexedNotNull { index, column ->
    val first = (0 until 7).map { column.weekStart.plus(it) }.firstOrNull { it.day == 1 }
    first?.let { MonthLabel(index, it) }
}

/** The letters at the side of the calendar, in the order of the week that starts at [firstWeekday]. */
fun weekdayLetters(firstWeekday: DayOfWeek, narrowName: (DayOfWeek) -> String): List<String> =
    getWeekdaySequence(firstWeekday).map(narrowName)
