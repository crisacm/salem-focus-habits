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

import androidx.compose.runtime.mutableStateListOf

/** The screens that open above the habit list, besides the habit detail. */
enum class AppScreen { ABOUT, INTRO, SETTINGS }

/** The screens shown above the habit list, the last one opened on top. Back from one shows the one below. */
class ScreenNavigation {
    private val stack = mutableStateListOf<AppScreen>()

    /** Every screen open, the one on top last. */
    val openScreens: List<AppScreen> get() = stack.toList()

    /** The screen on top, if any. */
    val current: AppScreen? get() = stack.lastOrNull()

    /** Shows [screen] on top; one that is already open moves to the top instead of being repeated. */
    fun open(screen: AppScreen) {
        stack.remove(screen)
        stack.add(screen)
    }

    /** Closes [screen] wherever it is in the stack. */
    fun close(screen: AppScreen) {
        stack.remove(screen)
    }
}
