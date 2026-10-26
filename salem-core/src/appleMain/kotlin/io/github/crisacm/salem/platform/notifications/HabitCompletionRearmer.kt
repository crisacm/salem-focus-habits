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

import io.github.crisacm.salem.core.commands.Command
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.commands.CreateRepetitionCommand
import io.github.crisacm.salem.core.reminders.ReminderScheduler
import io.github.crisacm.salem.platform.time.DateUtils
import io.github.crisacm.salem.platform.time.getToday

/**
 * On Android the alarm still fires when the habit is already done and the notification is simply
 * not shown. On iOS the pending notification would be shown anyway, so when today's habit is
 * completed the next reminder is moved to the following day, and unchecking brings today's back.
 * Register it with `CommandRunner.addListener`.
 *
 * @param midnightDelayHours the "midnight delay" preference, which moves the start of the habit day
 */
class HabitCompletionRearmer(
    private val reminderScheduler: ReminderScheduler,
    private val midnightDelayHours: () -> Int = { 0 }
) : CommandRunner.Listener {
    override fun onCommandFinished(command: Command) {
        if (command !is CreateRepetitionCommand) return
        val habit = command.habit
        val reminder = habit.reminder ?: return
        if (command.date != getToday()) return
        val completed = habit.reminderDoneToday()
        val upcoming = DateUtils.getUpcomingTimeInMillis(reminder.hour, reminder.minute)
        if (completed && isToday(upcoming)) {
            val tomorrow = DateUtils.applyTimezone(DateUtils.removeTimezone(upcoming) + DateUtils.DAY_LENGTH)
            reminderScheduler.scheduleAtTime(habit, tomorrow)
        } else {
            reminderScheduler.schedule(habit)
        }
    }

    // The habit day starts [midnightDelayHours] after the calendar day
    private fun isToday(time: Long): Boolean {
        val delay = midnightDelayHours() * DateUtils.HOUR_LENGTH
        val timeDay = DateUtils.getStartOfDay(DateUtils.removeTimezone(time) - delay)
        return timeDay == DateUtils.getStartOfDay(DateUtils.getLocalTime() - delay)
    }
}
