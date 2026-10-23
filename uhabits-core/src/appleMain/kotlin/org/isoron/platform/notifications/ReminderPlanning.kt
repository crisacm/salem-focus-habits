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

package org.isoron.platform.notifications

import org.isoron.platform.time.DateUtils
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.models.WeekdayList

private const val DAYS_FROM_1970_TO_2000 = 10_957L

/**
 * The times at which the reminder fires during the next [horizonDays] days, counting the day of
 * [reminderTime] (milliseconds since the Unix epoch) as the first: the same local time of day on
 * every day that is enabled in [days]. Android shows every alarm and checks the weekday when it
 * fires and arms the next alarm itself; iOS cannot run code at that moment, so the days are chosen
 * here and scheduled ahead.
 *
 * The weekday index is the one of `ShowNotificationTask`: index 0 is Saturday and index 1 is Sunday.
 */
internal fun upcomingReminderTimes(reminderTime: Long, days: WeekdayList, horizonDays: Int): List<Long> {
    val enabled = days.toArray()
    val localTime = DateUtils.removeTimezone(reminderTime)
    val times = mutableListOf<Long>()
    for (offset in 0 until horizonDays) {
        val shifted = localTime + offset * DateUtils.DAY_LENGTH
        val date = LocalDate((shifted.floorDiv(DateUtils.DAY_LENGTH) - DAYS_FROM_1970_TO_2000).toInt())
        if (enabled[(date.dayOfWeek.daysSinceSunday + 1) % 7]) {
            times.add(if (offset == 0) reminderTime else DateUtils.applyTimezone(shifted))
        }
    }
    return times
}

/**
 * The first of [upcomingReminderTimes] within a week, or null if no day is enabled.
 */
internal fun nextEnabledReminderTime(reminderTime: Long, days: WeekdayList): Long? =
    upcomingReminderTimes(reminderTime, days, 7).firstOrNull()

internal data class PendingReminder(val identifier: String, val fireTimeMillis: Long)

/**
 * iOS keeps at most 64 pending local notifications and silently discards the rest. Returns the
 * identifiers to remove so that only the [limit] earliest reminders stay.
 */
internal fun reminderIdsToDrop(pending: List<PendingReminder>, limit: Int): List<String> {
    if (pending.size <= limit) return emptyList()
    val ordered = pending.sortedWith(compareBy({ it.fireTimeMillis }, { it.identifier }))
    return ordered.drop(limit).map { it.identifier }
}
