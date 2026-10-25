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

package io.github.crisacm.salem.core.ui.screens.habits.edit

import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.commands.CreateHabitCommand
import io.github.crisacm.salem.core.commands.EditHabitCommand
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.ModelFactory

enum class SaveResult {
    /** The command was sent to the runner; the screen can close. */
    SAVED,

    /** The habit being edited no longer exists; nothing was saved. */
    HABIT_NOT_FOUND,

    /** A previous save is already running: a second tap on Save. */
    ALREADY_SAVING
}

/** Saves the form of the edit screen as a new habit or over an existing one, once per screen. */
class HabitEditorSaver(
    private val modelFactory: ModelFactory,
    private val habitList: HabitList,
    private val commandRunner: CommandRunner
) {
    private var saving = false

    /** Creates a habit from [form], or edits the one with [habitId] if it is not null. */
    fun save(form: EditHabitForm, habitId: Long?): SaveResult {
        if (saving) return SaveResult.ALREADY_SAVING
        val habit = modelFactory.buildHabit()
        val command = if (habitId != null) {
            val original = habitList.getById(habitId) ?: return SaveResult.HABIT_NOT_FOUND
            habit.copyFrom(original)
            form.applyTo(habit)
            EditHabitCommand(habitList, habitId, habit)
        } else {
            form.applyTo(habit)
            CreateHabitCommand(modelFactory, habitList, habit)
        }
        saving = true
        commandRunner.run(command)
        return SaveResult.SAVED
    }
}
