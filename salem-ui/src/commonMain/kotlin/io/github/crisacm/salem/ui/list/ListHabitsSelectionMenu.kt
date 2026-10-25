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

package io.github.crisacm.salem.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.NotificationTray
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior
import io.github.crisacm.salem.platform.time.getToday

/**
 * State and actions of the bar that replaces the top bar while habits are selected. The controller of
 * the list tells it when the selection starts, changes and finishes.
 */
class ListHabitsSelectionMenu(
    private val listState: HabitListState,
    var commandRunner: CommandRunner,
    private val prefs: Preferences,
    private val behavior: ListHabitsSelectionMenuBehavior,
    private val listController: Lazy<HabitListController>,
    private val notificationTray: NotificationTray
) : SelectionListener {
    var active by mutableStateOf(false)
        private set
    var selectedCount by mutableIntStateOf(0)
        private set
    var actions by mutableStateOf(SelectionActions.NONE)
        private set

    override fun onSelectionStart() {
        active = true
        refresh()
    }

    override fun onSelectionChange() = refresh()

    override fun onSelectionFinish() {
        active = false
        selectedCount = 0
        actions = SelectionActions.NONE
    }

    /** Ends the selection from the bar (its close button or the back button). */
    fun cancel() = listController.value.onSelectionFinished()

    fun onEdit() = behavior.onEditHabits()

    fun onColor() = behavior.onChangeColor()

    fun onArchive() = behavior.onArchiveHabits()

    fun onUnarchive() = behavior.onUnarchiveHabits()

    fun onDelete() = behavior.onDeleteHabits()

    fun onNotify() {
        for (habit in listState.getSelected()) notificationTray.show(habit, getToday(), 0)
    }

    private fun refresh() {
        selectedCount = listState.selectedIds.size
        actions = SelectionActions(
            canEdit = behavior.canEdit(),
            canArchive = behavior.canArchive(),
            canUnarchive = behavior.canUnarchive(),
            showNotify = prefs.isDeveloper
        )
    }
}
