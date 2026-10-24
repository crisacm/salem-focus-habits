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

package io.github.crisacm.salem.activities.habits.show

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitState
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.utils.StyledResources

/**
 * What the detail screen keeps outside of its composition: the state of the cards, what they ask of the
 * presenters, what the menu of the bar offers and the colour of the bar. The activity writes to it on every
 * refresh, so the menu follows the habit (for example Archive turns into Unarchive).
 */
class ShowHabitHost(
    private val context: Context,
    val menuPresenter: ShowHabitMenuPresenter,
    preferences: Preferences
) {
    var state by mutableStateOf<ShowHabitState?>(null)
        private set
    var callbacks by mutableStateOf(ShowHabitCallbacks())
    var canArchive by mutableStateOf(menuPresenter.canArchive())
        private set
    var canUnarchive by mutableStateOf(menuPresenter.canUnarchive())
        private set
    val isDeveloper = preferences.isDeveloper

    fun onState(newState: ShowHabitState) {
        canArchive = menuPresenter.canArchive()
        canUnarchive = menuPresenter.canUnarchive()
        state = newState
    }

    /** The colour of the top bar for [state]: the habit colour or the primary colour, as the old toolbar did. */
    fun barColor(state: ShowHabitState): Color {
        val styled = StyledResources(context)
        return Color(
            barColorFor(
                useHabitColor = styled.getBoolean(R.attr.useHabitColorAsPrimary),
                habitColor = state.theme.color(state.color).toInt(),
                primaryColor = styled.getColor(R.attr.colorPrimary)
            )
        )
    }

    // Before Android 15 the window is not edge to edge, so the status bar needs the colour of the bar.
    @Suppress("DEPRECATION")
    fun paintStatusBar(activity: Activity, color: Color) {
        activity.window.statusBarColor = color.toArgb()
    }
}
