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
import org.isoron.platform.time.currentTimeMillis
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.reminders.ReminderScheduler.SchedulerResult
import org.isoron.uhabits.core.reminders.ReminderScheduler.SystemScheduler
import platform.Foundation.NSLog

/**
 * [SystemScheduler] over local notifications. It keeps one pending notification per habit and at
 * most [limit] of them (iOS allows 64). Call [clear] before `ReminderScheduler.scheduleAll()` so
 * that reminders of habits that no longer have one are removed.
 *
 * @param defaultBody text of the notification for habits without a question
 */
class AppleSystemScheduler(
    private val center: PendingNotificationCenter,
    private val defaultBody: String,
    private val clock: () -> Long = ::currentTimeMillis,
    private val logger: (String) -> Unit = { NSLog("%@", it) },
    private val limit: Int = MAX_PENDING_NOTIFICATIONS
) : SystemScheduler {
    private val scheduled = LinkedHashMap<String, Long>()

    override fun scheduleShowReminder(reminderTime: Long, habit: Habit, timestamp: Long): SchedulerResult {
        val habitId = habit.id ?: return SchedulerResult.IGNORED
        val reminder = habit.reminder ?: return SchedulerResult.IGNORED
        val identifier = identifierOf(habitId)
        val fireTime = nextEnabledReminderTime(reminderTime, reminder.days)
        if (fireTime == null || fireTime <= clock()) {
            remove(identifier)
            return SchedulerResult.IGNORED
        }
        val day = if (fireTime == reminderTime) timestamp else DateUtils.getStartOfDayWithOffset(DateUtils.removeTimezone(fireTime), 0, 0)
        center.add(
            ReminderRequest(
                identifier,
                habit.name,
                habit.question.ifBlank { defaultBody },
                fireTime,
                habitId,
                day,
                habit.type == HabitType.YES_NO
            )
        )
        scheduled[identifier] = fireTime
        val dropped = reminderIdsToDrop(scheduled.map { PendingReminder(it.key, it.value) }, limit)
        for (id in dropped) {
            log("Over the limit of $limit notifications, dropping $id")
            remove(id)
        }
        return if (identifier in dropped) SchedulerResult.IGNORED else SchedulerResult.OK
    }

    override fun scheduleWidgetUpdate(updateTime: Long): SchedulerResult? = null

    override fun log(componentName: String, msg: String) = logger("$componentName: $msg")

    /**
     * Removes every pending reminder.
     */
    fun clear() {
        scheduled.clear()
        center.removeAll()
    }

    private fun remove(identifier: String) {
        scheduled.remove(identifier)
        center.remove(identifier)
    }

    private fun log(msg: String) = logger("AppleSystemScheduler: $msg")

    companion object {
        const val MAX_PENDING_NOTIFICATIONS = 64

        fun identifierOf(habitId: Long) = "reminder-$habitId"
    }
}
