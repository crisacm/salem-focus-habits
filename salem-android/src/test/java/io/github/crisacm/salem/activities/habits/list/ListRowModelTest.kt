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

import io.github.crisacm.salem.core.models.Entry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ListRowModelTest {
    @Test
    fun tapWithoutSelectionOpensTheHabit() {
        val result = rowTap(selected = emptyList(), id = 5)
        assertEquals(RowEvent.OPEN, result.event)
        assertEquals(emptyList<Long>(), result.selected)
    }

    @Test
    fun longPressWithoutSelectionStartsIt() {
        val result = rowLongPress(selected = emptyList(), id = 5)
        assertEquals(RowEvent.SELECTION_START, result.event)
        assertEquals(listOf(5L), result.selected)
    }

    @Test
    fun tapWithSelectionAddsTheRow() {
        val result = rowTap(selected = listOf(5), id = 7)
        assertEquals(RowEvent.SELECTION_CHANGE, result.event)
        assertEquals(listOf(5L, 7L), result.selected)
    }

    @Test
    fun longPressWithSelectionAddsTheRow() {
        val result = rowLongPress(selected = listOf(5), id = 7)
        assertEquals(RowEvent.SELECTION_CHANGE, result.event)
        assertEquals(listOf(5L, 7L), result.selected)
    }

    @Test
    fun tapOnSelectedRowRemovesIt() {
        val result = rowTap(selected = listOf(5, 7), id = 5)
        assertEquals(RowEvent.SELECTION_CHANGE, result.event)
        assertEquals(listOf(7L), result.selected)
    }

    @Test
    fun removingTheLastSelectedRowFinishesTheSelection() {
        val tap = rowTap(selected = listOf(5), id = 5)
        assertEquals(RowEvent.SELECTION_FINISH, tap.event)
        assertEquals(emptyList<Long>(), tap.selected)

        val longPress = rowLongPress(selected = listOf(5), id = 5)
        assertEquals(RowEvent.SELECTION_FINISH, longPress.event)
        assertEquals(emptyList<Long>(), longPress.selected)
    }

    @Test
    fun selectionKeepsTheOrderOfSelection() {
        val result = rowTap(selected = listOf(9, 2), id = 4)
        assertEquals(listOf(9L, 2L, 4L), result.selected)
    }

    @Test
    fun reorderMovesTheRowLikeTheCache() {
        val ids = listOf(1L, 2L, 3L, 4L)
        assertEquals(listOf(2L, 3L, 1L, 4L), reorderedIds(ids, from = 0, to = 2))
        assertEquals(listOf(1L, 4L, 2L, 3L), reorderedIds(ids, from = 3, to = 1))
    }

    @Test
    fun reorderDoesNotChangeTheOriginalList() {
        val ids = listOf(1L, 2L, 3L)
        reorderedIds(ids, from = 0, to = 2)
        assertEquals(listOf(1L, 2L, 3L), ids)
    }

    @Test
    fun reorderIgnoresNoMovesAndOutOfRangeIndexes() {
        val ids = listOf(1L, 2L, 3L)
        assertNull(reorderedIds(ids, from = 1, to = 1))
        assertNull(reorderedIds(ids, from = -1, to = 1))
        assertNull(reorderedIds(ids, from = 0, to = 3))
    }

    @Test
    fun dragTargetFollowsTheDraggedRow() {
        // rows of 50 px; row 1 starts at 50 and is dragged 60 px down: its centre is at 135
        assertEquals(2, dragTargetIndex(index = 1, offsetPx = 60f, rowHeightPx = 50f, count = 5))
        assertEquals(1, dragTargetIndex(index = 1, offsetPx = 20f, rowHeightPx = 50f, count = 5))
        assertEquals(0, dragTargetIndex(index = 1, offsetPx = -40f, rowHeightPx = 50f, count = 5))
    }

    @Test
    fun dragTargetStaysInsideTheList() {
        assertEquals(0, dragTargetIndex(index = 1, offsetPx = -500f, rowHeightPx = 50f, count = 5))
        assertEquals(4, dragTargetIndex(index = 3, offsetPx = 500f, rowHeightPx = 50f, count = 5))
    }

    @Test
    fun daysBeyondTheDataAreUnknown() {
        val values = intArrayOf(Entry.YES_MANUAL, Entry.NO)
        assertEquals(Entry.YES_MANUAL, checkmarkAt(values, 0))
        assertEquals(Entry.NO, checkmarkAt(values, 1))
        assertEquals(Entry.UNKNOWN, checkmarkAt(values, 2))
        assertEquals(Entry.UNKNOWN, checkmarkAt(values, -1))
    }

    @Test
    fun numbersAreStoredTimesOneThousandAndDefaultToZero() {
        val values = intArrayOf(2500, 0)
        assertEquals(2.5, numberAt(values, 0), 0.0)
        assertEquals(0.0, numberAt(values, 1), 0.0)
        assertEquals(0.0, numberAt(values, 5), 0.0)
    }

    @Test
    fun notesAndCompletionDefaultToNothing() {
        assertEquals("a", noteAt(arrayOf("a"), 0))
        assertEquals("", noteAt(arrayOf("a"), 1))
        assertEquals(true, completedAt(booleanArrayOf(true), 0))
        assertEquals(false, completedAt(booleanArrayOf(true), 1))
    }
}
