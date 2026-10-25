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

import io.github.crisacm.salem.core.ui.screens.habits.list.HintList

enum class EmptyListState { HIDDEN, EMPTY, DONE }

/** Nothing to show: no habits at all gives [EmptyListState.EMPTY], habits all hidden by the filter give [EmptyListState.DONE]. */
fun emptyListState(itemCount: Int, hasNoHabit: Boolean) = when {
    itemCount > 0 -> EmptyListState.HIDDEN
    hasNoHabit -> EmptyListState.EMPTY
    else -> EmptyListState.DONE
}

/** The hint to show now, if it is time for one and there is one left; reading it marks it as shown. */
fun nextHint(hintList: HintList): String? = if (hintList.shouldShow()) hintList.pop() else null
