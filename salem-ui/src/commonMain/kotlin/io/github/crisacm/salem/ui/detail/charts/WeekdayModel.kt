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

import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.getWeekdaySequence
import kotlin.math.roundToInt

/** The completions of a habit on one day of the week. */
class WeekdayBar(val day: DayOfWeek, val total: Int)

/**
 * The totals per day of the week, in the order of the week that starts at [firstWeekday]. [frequency] has, for
 * each month, the counts per day of the week starting at Saturday; numerical habits store thousandths.
 */
fun weekdayBars(frequency: Map<LocalDate, Array<Int>>, isNumerical: Boolean, firstWeekday: DayOfWeek): List<WeekdayBar> {
    val sums = IntArray(7)
    for (month in frequency.values) for (index in 0 until 7) sums[index] += month[index]
    val saturdayFirst = getWeekdaySequence(DayOfWeek.SATURDAY)
    return getWeekdaySequence(firstWeekday).map { day ->
        val sum = sums[saturdayFirst.indexOf(day)]
        WeekdayBar(day, if (isNumerical) (sum / 1000.0).roundToInt() else sum)
    }
}

/** The position of the day with the most completions (the first one on a tie), or -1 when there are none. */
fun highlightedWeekday(bars: List<WeekdayBar>): Int {
    val max = bars.maxOfOrNull { it.total } ?: return -1
    return if (max <= 0) -1 else bars.indexOfFirst { it.total == max }
}

/** The part of the width that the bar of [total] fills when the longest one is [max]. */
fun barFraction(total: Int, max: Int): Float = if (max <= 0) 0f else (total.toFloat() / max).coerceIn(0f, 1f)
