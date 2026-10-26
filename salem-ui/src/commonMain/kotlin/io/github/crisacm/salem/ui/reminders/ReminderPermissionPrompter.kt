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

package io.github.crisacm.salem.ui.reminders

import io.github.crisacm.salem.core.commands.Command
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.commands.CreateHabitCommand
import io.github.crisacm.salem.core.commands.EditHabitCommand
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Asks for the permission to show notifications at the moment the user wants one: when a habit with a reminder is
 * saved, and when the app starts with reminders already set (for example after an import). [request] is the platform
 * call; it must be safe to repeat, as the system only shows its question the first time. A failing request is not an
 * error for the command that was saved.
 */
class ReminderPermissionPrompter(
    private val request: suspend () -> Boolean,
    private val scope: CoroutineScope
) : CommandRunner.Listener {
    override fun onCommandFinished(command: Command) {
        val hasReminder = when (command) {
            is CreateHabitCommand -> command.model.hasReminder()
            is EditHabitCommand -> command.modified.hasReminder()
            else -> false
        }
        if (hasReminder) ask()
    }

    /** The app started; [hasReminders] tells whether any habit has a reminder. */
    fun onLaunch(hasReminders: Boolean) {
        if (hasReminders) ask()
    }

    private fun ask() {
        scope.launch {
            try {
                request()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Nothing to do: the reminders are still scheduled and the user can allow them in the settings
            }
        }
    }
}
