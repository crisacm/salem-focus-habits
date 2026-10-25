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

/** How the days of a reminder are summed up; [Custom] lists the short names of the days. */
sealed class WeekdaysSummary {
    data class Single(val name: String) : WeekdaysSummary()
    data object Weekends : WeekdaysSummary()
    data object AnyWeekday : WeekdaysSummary()
    data object AnyDay : WeekdaysSummary()
    data class Custom(val text: String) : WeekdaysSummary()
}

/** [shortNames] and [longNames] start at Saturday, as a [WeekdayList] does. */
fun weekdaysSummary(days: WeekdayList, shortNames: List<String>, longNames: List<String>): WeekdaysSummary {
    val selected = days.toArray()
    val indexes = selected.indices.filter { selected[it] }
    return when {
        indexes.size == 1 -> WeekdaysSummary.Single(longNames[indexes.first()])
        indexes.size == 2 && selected[0] && selected[1] -> WeekdaysSummary.Weekends
        indexes.size == 5 && !selected[0] && !selected[1] -> WeekdaysSummary.AnyWeekday
        indexes.size == 7 -> WeekdaysSummary.AnyDay
        else -> WeekdaysSummary.Custom(indexes.joinToString(", ") { shortNames[it] })
    }
}

/** The denominator of the choice picked in the frequency list of a numerical habit: every day, week or month. */
fun denominatorForChoice(index: Int): Int = when (index) {
    1 -> 7
    2 -> 30
    else -> 1
}

enum class NumericalFrequencyKind { EVERY_DAY, EVERY_WEEK, EVERY_MONTH, OTHER }

fun numericalFrequencyKind(denominator: Int) = when (denominator) {
    1 -> NumericalFrequencyKind.EVERY_DAY
    7 -> NumericalFrequencyKind.EVERY_WEEK
    30 -> NumericalFrequencyKind.EVERY_MONTH
    else -> NumericalFrequencyKind.OTHER
}

private const val DEFAULT_REMINDER_HOUR = 8

/** The time the clock starts at: the reminder of the form, or 08:00 while it has none. */
fun reminderStartTime(hour: Int, minute: Int): Pair<Int, Int> =
    if (hour >= 0) hour to maxOf(minute, 0) else DEFAULT_REMINDER_HOUR to 0

/** Whether a date pattern (such as the one of the template `j`) shows the hours from 0 to 23. */
fun uses24Hour(pattern: String): Boolean {
    val letters = pattern.split('\'').filterIndexed { index, _ -> index % 2 == 0 }.joinToString("")
    return 'H' in letters || 'k' in letters
}
