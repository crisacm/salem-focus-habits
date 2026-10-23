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
package io.github.crisacm.salem.core.ui.widgets

import me.tatarka.inject.annotations.Inject
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.commands.CreateRepetitionCommand
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.Entry.Companion.nextToggleValue
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.NotificationTray

@Inject
class WidgetBehavior(
    private val habitList: HabitList,
    private val commandRunner: CommandRunner,
    private val notificationTray: NotificationTray,
    private val preferences: Preferences
) {
    fun onAddRepetition(habit: Habit, date: LocalDate) {
        notificationTray.cancel(habit)
        val entry = habit.originalEntries.get(date)
        setValue(habit, date, Entry.YES_MANUAL, entry.notes)
    }

    fun onRemoveRepetition(habit: Habit, date: LocalDate) {
        notificationTray.cancel(habit)
        val entry = habit.originalEntries.get(date)
        setValue(habit, date, Entry.NO, entry.notes)
    }

    fun onToggleRepetition(habit: Habit, date: LocalDate) {
        val entry = habit.originalEntries.get(date)
        val currentValue = entry.value
        val newValue = nextToggleValue(
            value = currentValue,
            isSkipEnabled = preferences.isSkipEnabled,
            areQuestionMarksEnabled = preferences.areQuestionMarksEnabled
        )
        setValue(habit, date, newValue, entry.notes)
        notificationTray.cancel(habit)
    }

    fun onIncrement(habit: Habit, date: LocalDate, amount: Int) {
        val entry = habit.computedEntries.get(date)
        val currentValue = entry.value
        setValue(habit, date, currentValue + amount, entry.notes)
        notificationTray.cancel(habit)
    }

    fun onDecrement(habit: Habit, date: LocalDate, amount: Int) {
        val entry = habit.computedEntries.get(date)
        val currentValue = entry.value
        setValue(habit, date, currentValue - amount, entry.notes)
        notificationTray.cancel(habit)
    }

    fun setValue(habit: Habit, date: LocalDate, newValue: Int, notes: String) {
        commandRunner.run(
            CreateRepetitionCommand(habitList, habit, date, newValue, notes)
        )
    }
}
