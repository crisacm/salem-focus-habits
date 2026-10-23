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

package io.github.crisacm.salem.activities.habits.list

import io.github.crisacm.salem.core.models.HabitList.Order
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListBarModelTest {
    @Test
    fun hideCompletedSaysEnteredWhenQuestionMarksOrSkipAreOn() {
        assertTrue(hideCompletedIsEntered(questionMarksEnabled = true, skipEnabled = false))
        assertTrue(hideCompletedIsEntered(questionMarksEnabled = false, skipEnabled = true))
        assertTrue(hideCompletedIsEntered(questionMarksEnabled = true, skipEnabled = true))
        assertFalse(hideCompletedIsEntered(questionMarksEnabled = false, skipEnabled = false))
    }

    @Test
    fun ascendingOrdersPointDown() {
        assertEquals(SortMark.DOWN, sortMark(Order.BY_NAME_ASC, SortItem.NAME))
        assertEquals(SortMark.DOWN, sortMark(Order.BY_COLOR_ASC, SortItem.COLOR))
        assertEquals(SortMark.DOWN, sortMark(Order.BY_SCORE_ASC, SortItem.SCORE))
        assertEquals(SortMark.DOWN, sortMark(Order.BY_STATUS_ASC, SortItem.STATUS))
    }

    @Test
    fun descendingOrdersPointUp() {
        assertEquals(SortMark.UP, sortMark(Order.BY_NAME_DESC, SortItem.NAME))
        assertEquals(SortMark.UP, sortMark(Order.BY_COLOR_DESC, SortItem.COLOR))
        assertEquals(SortMark.UP, sortMark(Order.BY_SCORE_DESC, SortItem.SCORE))
        assertEquals(SortMark.UP, sortMark(Order.BY_STATUS_DESC, SortItem.STATUS))
    }

    @Test
    fun theManualOrderPointsUp() {
        assertEquals(SortMark.UP, sortMark(Order.BY_POSITION, SortItem.MANUAL))
    }

    @Test
    fun onlyTheCurrentOrderIsMarked() {
        SortItem.values().filter { it != SortItem.NAME }.forEach {
            assertEquals(SortMark.NONE, sortMark(Order.BY_NAME_ASC, it))
        }
        assertEquals(SortMark.NONE, sortMark(Order.BY_POSITION, SortItem.NAME))
    }

    @Test
    fun theSelectionActionsFollowTheSelection() {
        val one = SelectionActions(canEdit = true, canArchive = true, canUnarchive = false, showNotify = false)
        assertTrue(one.canEdit)
        assertFalse(one.canUnarchive)
        assertFalse(SelectionActions.NONE.canEdit)
        assertFalse(SelectionActions.NONE.canArchive)
        assertFalse(SelectionActions.NONE.canUnarchive)
        assertFalse(SelectionActions.NONE.showNotify)
    }
}
