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

package io.github.crisacm.salem.ui.settings

import io.github.crisacm.salem.platform.time.DayOfWeek

class WeekdayOption(val value: String, val name: String)

private val WEEKDAY_VALUES = listOf("7", "1", "2", "3", "4", "5", "6")

/** [dayNames] must start at Saturday, as the stored values are Sunday-based (Sunday = 1). */
fun firstWeekdayOptions(dayNames: List<String>) =
    WEEKDAY_VALUES.zip(dayNames) { value, name -> WeekdayOption(value, name) }

fun firstWeekdaySummary(dayNames: List<String>, sundayBasedWeekday: Int) =
    dayNames[sundayBasedWeekday % 7]

/** The day for a stored Sunday-based number ("1" is Sunday, "7" is Saturday). */
fun weekdayFromNumber(number: String): DayOfWeek =
    DayOfWeek.entries.first { it.daysSinceSunday + 1 == number.toInt() }
