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

import io.github.crisacm.salem.core.commands.ChangeHabitColorCommand
import io.github.crisacm.salem.core.commands.Command
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.commands.CreateRepetitionCommand
import io.github.crisacm.salem.core.reminders.ReminderScheduler

/**
 * Keeps the pending notifications in line with the habits after every command. On Android a
 * reminder that no longer applies (the habit was deleted, archived or lost its reminder) is simply
 * not shown when its alarm fires; on iOS the notification would still be delivered, and
 * `ReminderScheduler.scheduleAll()` alone never removes it. So after each command the pending
 * notifications are cleared and scheduled again.
 *
 * Use it instead of `ReminderScheduler.startListening()`; checking a habit is handled by
 * [HabitCompletionRearmer] and changing a colour does not affect reminders.
 */
class AppleReminderSync(
    private val commandRunner: CommandRunner,
    private val reminderScheduler: ReminderScheduler,
    private val systemScheduler: AppleSystemScheduler
) : CommandRunner.Listener {
    fun startListening() = commandRunner.addListener(this)

    fun stopListening() = commandRunner.removeListener(this)

    override fun onCommandFinished(command: Command) {
        if (command is CreateRepetitionCommand || command is ChangeHabitColorCommand) return
        systemScheduler.clear()
        reminderScheduler.scheduleAll()
    }
}
