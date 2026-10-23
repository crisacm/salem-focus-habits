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

package org.isoron.platform.time

/**
 * Milliseconds since the Unix epoch of a UTC date and time. The month is 1-based.
 */
fun utcTime(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long {
    val days = LocalDate(year, month, day).daysSince2000 + 10_957L
    return days * DateUtils.DAY_LENGTH + hour * DateUtils.HOUR_LENGTH + minute * DateUtils.MINUTE_LENGTH
}
