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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsMenuBehavior

/** What the filter, sort and overflow menus show; it is read again every time a menu is opened. */
class MenuSnapshot(
    val hideArchived: Boolean,
    val hideCompleted: Boolean,
    val hideCompletedIsEntered: Boolean,
    val order: HabitList.Order,
    val nightMode: Boolean
)

/** State and actions of the top bar of the habit list, shown by [ListHabitsBar]. */
class ListHabitsMenu(
    private val preferences: Preferences,
    private val themeSwitcher: ThemeSwitcher,
    val behavior: ListHabitsMenuBehavior
) {
    var searchActive by mutableStateOf(false)
        private set
    var searchQuery by mutableStateOf(behavior.searchQuery)
        private set

    fun snapshot() = MenuSnapshot(
        hideArchived = !preferences.showArchived,
        hideCompleted = !preferences.showCompleted,
        hideCompletedIsEntered = hideCompletedIsEntered(preferences.areQuestionMarksEnabled, preferences.isSkipEnabled),
        order = preferences.defaultPrimaryOrder,
        nightMode = themeSwitcher.isNightMode
    )

    fun startSearch() {
        searchActive = true
    }

    fun onSearchChange(query: String) {
        searchQuery = query
        behavior.onSearchQueryChanged(query)
    }

    fun closeSearch() {
        searchActive = false
        onSearchChange("")
    }

    fun onCreateHabit() = behavior.onCreateHabit()

    fun onToggleNightMode() = behavior.onToggleNightMode()

    fun onSettings() = behavior.onViewSettings()

    fun onHelp() = behavior.onViewFAQ()

    fun onAbout() = behavior.onViewAbout()

    fun onToggleHideArchived() = behavior.onToggleShowArchived()

    fun onToggleHideCompleted() = behavior.onToggleShowCompleted()

    fun onSort(item: SortItem) = when (item) {
        SortItem.MANUAL -> behavior.onSortByManually()
        SortItem.NAME -> behavior.onSortByName()
        SortItem.COLOR -> behavior.onSortByColor()
        SortItem.SCORE -> behavior.onSortByScore()
        SortItem.STATUS -> behavior.onSortByStatus()
    }
}
