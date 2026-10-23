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

import org.isoron.platform.Synchronized
import org.isoron.platform.time.DateUtils
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.currentTimeMillis
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.reminders.ReminderScheduler.SchedulerResult
import org.isoron.uhabits.core.reminders.ReminderScheduler.SystemScheduler
import platform.Foundation.NSLog

/**
 * [SystemScheduler] over local notifications. iOS cannot run code when a notification fires, so
 * instead of arming one alarm at a time as Android does, it schedules ahead one notification for
 * each enabled day of the next [horizonDays] days (`reminder-<habitId>-<day>`), and re-schedules
 * them whenever the app runs. At most [limit] are kept across all habits (iOS allows 64): the
 * earliest ones, so with many habits the horizon gets shorter. Call [clear] before
 * `ReminderScheduler.scheduleAll()` so that reminders of habits that no longer have one are removed.
 *
 * @param defaultBody text of the notification for habits without a question
 */
class AppleSystemScheduler(
    private val center: PendingNotificationCenter,
    private val defaultBody: String,
    private val clock: () -> Long = ::currentTimeMillis,
    private val logger: (String) -> Unit = { NSLog("%@", it) },
    private val limit: Int = MAX_PENDING_NOTIFICATIONS,
    private val horizonDays: Int = DEFAULT_HORIZON_DAYS
) : SystemScheduler {
    private val scheduled = LinkedHashMap<Long, MutableList<PendingReminder>>()

    @Synchronized
    override fun scheduleShowReminder(reminderTime: Long, habit: Habit, timestamp: Long): SchedulerResult {
        val habitId = habit.id ?: return SchedulerResult.IGNORED
        val reminder = habit.reminder ?: return SchedulerResult.IGNORED
        removeHabit(habitId)
        // A snoozed time is a one-off: only the regular time of the reminder repeats every day
        val days = if (isRegularTime(reminderTime, reminder.hour, reminder.minute)) horizonDays else 1
        val now = clock()
        val times = upcomingReminderTimes(reminderTime, reminder.days, days).filter { it > now }
        if (times.isEmpty()) return SchedulerResult.IGNORED
        val pending = scheduled.getOrPut(habitId) { mutableListOf() }
        for (time in times) {
            val dayStart = if (time == reminderTime) timestamp else startOfLocalDay(time)
            val request = ReminderRequest(
                identifierOf(habitId, localDateOf(time)),
                habit.name,
                habit.question.ifBlank { defaultBody },
                time,
                habitId,
                dayStart,
                habit.type == HabitType.YES_NO
            )
            center.add(request)
            pending.add(PendingReminder(request.identifier, time))
        }
        val dropped = dropOverTheLimit()
        return if (pending.any { it.identifier !in dropped }) SchedulerResult.OK else SchedulerResult.IGNORED
    }

    override fun scheduleWidgetUpdate(updateTime: Long): SchedulerResult? = null

    override fun log(componentName: String, msg: String) = logger("$componentName: $msg")

    /**
     * Removes every pending reminder.
     */
    @Synchronized
    fun clear() {
        scheduled.clear()
        center.removeAll()
    }

    private fun removeHabit(habitId: Long) {
        scheduled.remove(habitId)?.forEach { center.remove(it.identifier) }
    }

    private fun dropOverTheLimit(): Set<String> {
        val dropped = reminderIdsToDrop(scheduled.values.flatten(), limit).toSet()
        if (dropped.isEmpty()) return dropped
        log("Over the limit of $limit notifications, dropping ${dropped.size}: ${dropped.sorted().joinToString()}")
        for (id in dropped) center.remove(id)
        for (list in scheduled.values) list.removeAll { it.identifier in dropped }
        scheduled.values.removeAll { it.isEmpty() }
        return dropped
    }

    private fun isRegularTime(time: Long, hour: Int, minute: Int): Boolean {
        val timeOfDay = DateUtils.removeTimezone(time).mod(DateUtils.DAY_LENGTH)
        return timeOfDay == hour * DateUtils.HOUR_LENGTH + minute * DateUtils.MINUTE_LENGTH
    }

    private fun startOfLocalDay(time: Long) = DateUtils.getStartOfDay(DateUtils.removeTimezone(time))

    private fun localDateOf(time: Long) = LocalDate((startOfLocalDay(time) / DateUtils.DAY_LENGTH - DAYS_FROM_1970_TO_2000).toInt())

    private fun log(msg: String) = logger("AppleSystemScheduler: $msg")

    companion object {
        const val MAX_PENDING_NOTIFICATIONS = 64
        const val DEFAULT_HORIZON_DAYS = 7
        private const val DAYS_FROM_1970_TO_2000 = 10_957L

        fun identifierOf(habitId: Long) = "reminder-$habitId"

        fun identifierOf(habitId: Long, day: LocalDate) = "reminder-$habitId-${day.daysSince2000}"
    }
}
