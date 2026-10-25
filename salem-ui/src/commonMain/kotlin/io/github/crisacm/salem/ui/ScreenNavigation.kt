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

package io.github.crisacm.salem.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** The screens that open above the habit list, besides the habit detail. */
enum class AppScreen { ABOUT, INTRO }

/** The screen shown above the habit list, if any. Opening one replaces the one that was open. */
class ScreenNavigation {
    var current by mutableStateOf<AppScreen?>(null)
        private set

    fun open(screen: AppScreen) {
        current = screen
    }

    /** Closes [screen] when it is the one shown; a screen that was already replaced stays closed. */
    fun close(screen: AppScreen) {
        if (current == screen) current = null
    }
}
