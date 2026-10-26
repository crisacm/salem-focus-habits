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

package io.github.crisacm.salem.platform.notifications

import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.platform.time.DateUtils
import io.github.crisacm.salem.platform.time.LocalDate

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

/** The calendar day (device zone) of a reminder time in milliseconds since the Unix epoch. */
internal fun localDateOf(time: Long): LocalDate =
    LocalDate((DateUtils.removeTimezone(time).floorDiv(DateUtils.DAY_LENGTH) - DAYS_FROM_1970_TO_2000).toInt())

/** The notification of [habit] for [time]; [timestamp] is the start of the day it is about. */
internal fun reminderRequestFor(habit: Habit, habitId: Long, time: Long, timestamp: Long, defaultBody: String) = ReminderRequest(
    AppleSystemScheduler.identifierOf(habitId, localDateOf(time)),
    habit.name,
    habit.question.ifBlank { defaultBody },
    time,
    habitId,
    timestamp,
    habit.type == HabitType.YES_NO
)

/** What an app that is not running has to do with today's pending reminder after a habit changed. */
internal sealed interface TodayReminderChange {
    data object Nothing : TodayReminderChange

    data class Remove(val identifier: String) : TodayReminderChange

    data class Add(val request: ReminderRequest) : TodayReminderChange
}

/**
 * The shortcuts and the widget change habits in a process that has no `CommandRunner` listeners, so nothing moves the
 * reminder of today as `HabitCompletionRearmer` does in the app. A habit that is done loses its pending request of
 * today; one that is not done gets it back as long as its time has not passed and the weekday is enabled. The
 * identifier only depends on the habit and the day, so the request can be removed without knowing what the app scheduled.
 */
internal fun todayReminderChange(habit: Habit, now: Long, defaultBody: String): TodayReminderChange {
    val habitId = habit.id ?: return TodayReminderChange.Nothing
    val reminder = habit.reminder ?: return TodayReminderChange.Nothing
    val dayStart = DateUtils.getStartOfDay(DateUtils.removeTimezone(now))
    val time = DateUtils.applyTimezone(dayStart + reminder.hour * DateUtils.HOUR_LENGTH + reminder.minute * DateUtils.MINUTE_LENGTH)
    if (upcomingReminderTimes(time, reminder.days, 1).isEmpty()) return TodayReminderChange.Nothing
    if (habit.reminderDoneToday()) return TodayReminderChange.Remove(AppleSystemScheduler.identifierOf(habitId, localDateOf(time)))
    if (time <= now) return TodayReminderChange.Nothing
    return TodayReminderChange.Add(reminderRequestFor(habit, habitId, time, dayStart, defaultBody))
}

/**
 * Applies [todayReminderChange] to the pending notifications. Called by the extensions after they changed [habit];
 * [now] is in milliseconds since the Unix epoch and [defaultBody] is the text for habits without a question.
 */
fun updateTodayReminder(center: PendingNotificationCenter, habit: Habit, now: Long, defaultBody: String) {
    when (val change = todayReminderChange(habit, now, defaultBody)) {
        is TodayReminderChange.Remove -> center.remove(change.identifier)
        is TodayReminderChange.Add -> center.add(change.request)
        TodayReminderChange.Nothing -> Unit
    }
}
