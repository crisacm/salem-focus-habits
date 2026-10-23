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
 * Moves [reminderTime] (milliseconds since the Unix epoch) to the same local time on the first day
 * that is enabled in [days], or returns null if no day is enabled. Android shows every alarm and
 * checks the weekday when it fires; iOS cannot run code at that moment, so the day is chosen here.
 *
 * The weekday index is the one of `ShowNotificationTask`: index 0 is Saturday and index 1 is Sunday.
 */
internal fun nextEnabledReminderTime(reminderTime: Long, days: WeekdayList): Long? {
    val enabled = days.toArray()
    val localTime = DateUtils.removeTimezone(reminderTime)
    for (offset in 0..6) {
        val shifted = localTime + offset * DateUtils.DAY_LENGTH
        val date = LocalDate((shifted.floorDiv(DateUtils.DAY_LENGTH) - DAYS_FROM_1970_TO_2000).toInt())
        if (enabled[(date.dayOfWeek.daysSinceSunday + 1) % 7]) {
            return if (offset == 0) reminderTime else DateUtils.applyTimezone(shifted)
        }
    }
    return null
}

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
