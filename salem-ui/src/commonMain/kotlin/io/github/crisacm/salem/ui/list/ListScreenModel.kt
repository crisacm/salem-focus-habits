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

import androidx.compose.runtime.State
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.ui.screens.habits.list.HintList
import io.github.crisacm.salem.platform.time.LocalDateFormatter

/**
 * What [ListHabitsContent] needs from the platform: the state holders it draws, the names that
 * depend on the locale, and the moments in which the screen is shown and its width is known.
 */
interface ListScreenModel {
    val menu: ListHabitsMenu
    val selectionMenu: ListHabitsSelectionMenu
    val listState: HabitListState
    val headerState: HeaderState
    val rowCallbacks: ListRowCallbacks
    val preferences: Preferences
    val emptyState: State<EmptyListState>
    val hintList: HintList
    val taskRunner: TaskRunner
    val dateFormatter: LocalDateFormatter

    /** The screen entered the composition: start listening to the model. */
    fun onAttached()

    /** The screen left the composition. */
    fun onDetached()

    /** The width in pixels of the whole screen, which tells the model that the screen has a size. */
    fun onWidthChanged(width: Int)
}
