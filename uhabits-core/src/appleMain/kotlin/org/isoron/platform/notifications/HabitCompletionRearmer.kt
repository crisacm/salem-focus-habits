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
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.reminders.ReminderScheduler

/**
 * On Android the alarm still fires when the habit is already done and the notification is simply
 * not shown. On iOS the pending notification would be shown anyway, so when today's habit is
 * completed the next reminder is moved to the following day, and unchecking brings today's back.
 * Register it with `CommandRunner.addListener`.
 */
class HabitCompletionRearmer(
    private val reminderScheduler: ReminderScheduler
) : CommandRunner.Listener {
    override fun onCommandFinished(command: Command) {
        if (command !is CreateRepetitionCommand) return
        val habit = command.habit
        val reminder = habit.reminder ?: return
        if (command.date != getToday()) return
        val completed = habit.isCompletedToday() && habit.targetType != NumericalHabitType.AT_MOST
        val upcoming = DateUtils.getUpcomingTimeInMillis(reminder.hour, reminder.minute)
        if (completed && isToday(upcoming)) {
            val tomorrow = DateUtils.applyTimezone(DateUtils.removeTimezone(upcoming) + DateUtils.DAY_LENGTH)
            reminderScheduler.scheduleAtTime(habit, tomorrow)
        } else {
            reminderScheduler.schedule(habit)
        }
    }

    private fun isToday(time: Long): Boolean =
        DateUtils.getStartOfDay(DateUtils.removeTimezone(time)) == DateUtils.getStartOfDay(DateUtils.getLocalTime())
}
