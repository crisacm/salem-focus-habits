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

import io.github.crisacm.salem.core.models.HabitList.Order

enum class SortItem { MANUAL, NAME, COLOR, SCORE, STATUS }

enum class SortMark { NONE, UP, DOWN }

/** The title of the hide completed item changes when entries can be something other than done. */
fun hideCompletedIsEntered(questionMarksEnabled: Boolean, skipEnabled: Boolean) =
    questionMarksEnabled || skipEnabled

/** The arrow shown next to [item] of the sort menu: ascending orders point down, descending ones up. */
fun sortMark(current: Order, item: SortItem): SortMark = when (item) {
    SortItem.MANUAL -> if (current == Order.BY_POSITION) SortMark.UP else SortMark.NONE
    SortItem.NAME -> directional(current, Order.BY_NAME_ASC, Order.BY_NAME_DESC)
    SortItem.COLOR -> directional(current, Order.BY_COLOR_ASC, Order.BY_COLOR_DESC)
    SortItem.SCORE -> directional(current, Order.BY_SCORE_ASC, Order.BY_SCORE_DESC)
    SortItem.STATUS -> directional(current, Order.BY_STATUS_ASC, Order.BY_STATUS_DESC)
}

private fun directional(current: Order, ascending: Order, descending: Order) = when (current) {
    ascending -> SortMark.DOWN
    descending -> SortMark.UP
    else -> SortMark.NONE
}

/** Which actions the selection bar offers; delete is always there. Edit and colour need a single habit selected. */
data class SelectionActions(
    val canEdit: Boolean,
    val canArchive: Boolean,
    val canUnarchive: Boolean,
    val showNotify: Boolean,
    val canColor: Boolean = canEdit
) {
    companion object {
        val NONE = SelectionActions(canEdit = false, canArchive = false, canUnarchive = false, showNotify = false)
    }
}
