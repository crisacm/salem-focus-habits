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
package io.github.crisacm.salem.core.reminders

import io.github.crisacm.salem.core.AppScope
import io.github.crisacm.salem.core.commands.ChangeHabitColorCommand
import io.github.crisacm.salem.core.commands.Command
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.commands.CreateRepetitionCommand
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.HabitMatcher
import io.github.crisacm.salem.core.preferences.WidgetPreferences
import io.github.crisacm.salem.platform.Synchronized
import io.github.crisacm.salem.platform.time.DateUtils
import me.tatarka.inject.annotations.Inject

@AppScope
@Inject
open class ReminderScheduler(
    private val commandRunner: CommandRunner,
    private val habitList: HabitList,
    private val sys: SystemScheduler,
    private val widgetPreferences: WidgetPreferences
) : CommandRunner.Listener {
    @Synchronized
    override fun onCommandFinished(command: Command) {
        if (command is CreateRepetitionCommand) return
        if (command is ChangeHabitColorCommand) return
        scheduleAll()
    }

    @Synchronized
    open fun schedule(habit: Habit) {
        if (habit.id == null) {
            sys.log("ReminderScheduler", "Habit has null id. Returning.")
            return
        }
        if (!habit.hasReminder()) {
            sys.log("ReminderScheduler", "habit=" + habit.id + " has no reminder. Skipping.")
            return
        }
        var reminderTime = DateUtils.getUpcomingTimeInMillis(
            habit.reminder!!.hour,
            habit.reminder!!.minute
        )
        val snoozeReminderTime = widgetPreferences.getSnoozeTime(habit.id!!)
        if (snoozeReminderTime != 0L) {
            val now = DateUtils.applyTimezone(DateUtils.getLocalTime())
            sys.log("ReminderScheduler", "Habit ${habit.id} has been snoozed until $snoozeReminderTime")
            if (snoozeReminderTime > now) {
                sys.log("ReminderScheduler", "Snooze time is in the future. Accepting.")
                reminderTime = snoozeReminderTime
            } else {
                sys.log("ReminderScheduler", "Snooze time is in the past. Discarding.")
                widgetPreferences.removeSnoozeTime(habit.id!!)
            }
        }
        scheduleAtTime(habit, reminderTime)
    }

    @Synchronized
    open fun scheduleAtTime(habit: Habit, reminderTime: Long) {
        sys.log("ReminderScheduler", "Scheduling alarm for habit=" + habit.id)
        if (!habit.hasReminder()) {
            sys.log("ReminderScheduler", "habit=" + habit.id + " has no reminder. Skipping.")
            return
        }
        if (habit.isArchived) {
            sys.log("ReminderScheduler", "habit=" + habit.id + " is archived. Skipping.")
            return
        }
        val timestamp = DateUtils.getStartOfDayWithOffset(DateUtils.removeTimezone(reminderTime), 0, 0)
        sys.log("ReminderScheduler", "reminderTime=$reminderTime removeTimezone=${DateUtils.removeTimezone(reminderTime)} timestamp=$timestamp")
        sys.scheduleShowReminder(reminderTime, habit, timestamp)
    }

    @Synchronized
    open fun scheduleAll() {
        sys.log("ReminderScheduler", "Scheduling all alarms")
        val reminderHabits = habitList.getFiltered(HabitMatcher.WITH_ALARM)
        for (habit in reminderHabits) schedule(habit)
    }

    @Synchronized
    open fun hasHabitsWithReminders(): Boolean {
        return !habitList.getFiltered(HabitMatcher.WITH_ALARM).isEmpty
    }

    @Synchronized
    open fun startListening() {
        commandRunner.addListener(this)
    }

    @Synchronized
    open fun stopListening() {
        commandRunner.removeListener(this)
    }

    @Synchronized
    open fun snoozeReminder(habit: Habit, minutes: Long) {
        val now = DateUtils.applyTimezone(DateUtils.getLocalTime())
        val snoozedUntil = now + minutes * 60 * 1000
        widgetPreferences.setSnoozeTime(habit.id!!, snoozedUntil)
        schedule(habit)
    }

    interface SystemScheduler {
        fun scheduleShowReminder(
            reminderTime: Long,
            habit: Habit,
            timestamp: Long
        ): SchedulerResult

        fun scheduleWidgetUpdate(updateTime: Long): SchedulerResult?
        fun log(componentName: String, msg: String)
    }

    enum class SchedulerResult {
        IGNORED, OK
    }
}
