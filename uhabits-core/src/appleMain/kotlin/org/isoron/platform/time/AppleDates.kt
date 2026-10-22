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

import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone
import platform.Foundation.timeIntervalSince1970

private const val MILLIS_PER_DAY = 86_400_000L
private const val DAYS_FROM_1970_TO_2000 = 10_957

actual fun computeToday(hourOffset: Int, minuteOffset: Int): LocalDate {
    val now = NSDate()
    val nowMillis = (now.timeIntervalSince1970 * 1000).toLong()
    val zoneOffsetMillis = NSTimeZone.localTimeZone.secondsFromGMTForDate(now) * 1000L
    return LocalDate(daysSince2000(nowMillis, zoneOffsetMillis, hourOffset, minuteOffset))
}

/**
 * Same convention as Calendar.firstDayOfWeek on the JVM: 1 = Sunday ... 7 = Saturday.
 */
actual fun getFirstWeekdayNumberAccordingToLocale(): Int {
    return NSCalendar.autoupdatingCurrentCalendar.firstWeekday.toInt()
}

/**
 * Days since 2000-01-01 of the local date at [nowMillis] (milliseconds since the Unix epoch),
 * in a zone [zoneOffsetMillis] ahead of UTC, with the day starting [hourOffset] hours and
 * [minuteOffset] minutes late.
 */
internal fun daysSince2000(
    nowMillis: Long,
    zoneOffsetMillis: Long,
    hourOffset: Int,
    minuteOffset: Int
): Int {
    val offsetMillis = hourOffset * 3_600_000L + minuteOffset * 60_000L
    val adjustedMillis = nowMillis + zoneOffsetMillis - offsetMillis
    return (adjustedMillis.floorDiv(MILLIS_PER_DAY) - DAYS_FROM_1970_TO_2000).toInt()
}
