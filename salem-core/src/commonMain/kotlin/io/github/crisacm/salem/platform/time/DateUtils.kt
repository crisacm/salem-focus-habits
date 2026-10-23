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

package io.github.crisacm.salem.platform.time

object DateUtils {
    const val SECOND_LENGTH: Long = 1000
    const val MINUTE_LENGTH: Long = 60 * SECOND_LENGTH
    const val HOUR_LENGTH: Long = 60 * MINUTE_LENGTH
    const val DAY_LENGTH: Long = 24 * HOUR_LENGTH

    private var fixedLocalTime: Long? = null

    fun setFixedLocalTime(value: Long?) {
        fixedLocalTime = value
    }

    var fixedTimeZone: ZoneOffsets? = null
        private set

    fun setFixedTimeZone(value: ZoneOffsets?) {
        fixedTimeZone = value
    }

    private fun defaultZone(): ZoneOffsets = fixedTimeZone ?: systemZoneOffsets()

    fun applyTimezone(
        localTimestamp: Long,
        tz: ZoneOffsets = defaultZone()
    ): Long {
        return localTimestamp - tz.offsetAt(localTimestamp - tz.offsetAt(localTimestamp))
    }

    fun removeTimezone(
        timestamp: Long,
        tz: ZoneOffsets = defaultZone()
    ): Long {
        return timestamp + tz.offsetAt(timestamp)
    }

    fun getLocalTime(
        tz: ZoneOffsets = defaultZone(),
        utcTimeInMillis: Long? = null
    ): Long {
        fixedLocalTime?.let { return it }
        val now = utcTimeInMillis ?: currentTimeMillis()
        return now + tz.offsetAt(now)
    }

    fun getStartOfDay(timestamp: Long): Long = timestamp.floorDiv(DAY_LENGTH) * DAY_LENGTH

    fun getStartOfDayWithOffset(
        timestamp: Long,
        hourOffset: Int,
        minuteOffset: Int
    ): Long {
        val offset = hourOffset * HOUR_LENGTH + minuteOffset * MINUTE_LENGTH
        return getStartOfDay(timestamp - offset)
    }

    fun getStartOfTomorrowWithOffset(
        hourOffset: Int,
        minuteOffset: Int,
        tz: ZoneOffsets = defaultZone()
    ): Long = getUpcomingTimeInMillis(hourOffset, minuteOffset, tz)

    fun millisecondsUntilTomorrowWithOffset(
        hourOffset: Int,
        minuteOffset: Int,
        tz: ZoneOffsets = defaultZone()
    ): Long {
        return getStartOfTomorrowWithOffset(hourOffset, minuteOffset, tz) -
            applyTimezone(getLocalTime(tz), tz)
    }

    fun getUpcomingTimeInMillis(
        hour: Int,
        minute: Int,
        tz: ZoneOffsets = defaultZone()
    ): Long {
        val localTime = getLocalTime(tz)
        var time = getStartOfDay(localTime) + hour * HOUR_LENGTH + minute * MINUTE_LENGTH
        if (localTime > time) {
            time += DAY_LENGTH
        }
        return applyTimezone(time, tz)
    }
}
