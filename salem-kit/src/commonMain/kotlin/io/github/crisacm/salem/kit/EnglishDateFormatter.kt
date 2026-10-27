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

package io.github.crisacm.salem.kit

import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.LocalDateFormatter

/** Month and weekday names in English, for tests and for a session that has no platform formatter yet. */
class EnglishDateFormatter : LocalDateFormatter {
    override fun narrowWeekdayName(weekday: DayOfWeek) = weekday.name.take(1)
    override fun narrowWeekdayName(date: LocalDate) = narrowWeekdayName(date.dayOfWeek)
    override fun shortWeekdayName(weekday: DayOfWeek) = weekday.name.take(3)
    override fun shortWeekdayName(date: LocalDate) = shortWeekdayName(date.dayOfWeek)
    override fun shortMonthName(date: LocalDate) = MONTHS[date.month - 1]
    override fun longWeekdayName(weekday: DayOfWeek) = weekday.name
    override fun longMonthName(date: LocalDate) = MONTHS[date.month - 1]
    override fun longFormat(date: LocalDate) = "${MONTHS[date.month - 1]} ${date.day}, ${date.year}"

    private companion object {
        val MONTHS = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
    }
}
