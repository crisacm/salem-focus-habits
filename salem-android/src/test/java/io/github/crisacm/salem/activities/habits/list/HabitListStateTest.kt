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

import dev.mokkery.mock
import dev.mokkery.verify
import io.github.crisacm.salem.BaseAndroidJVMTest
import io.github.crisacm.salem.core.commands.DeleteHabitsCommand
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.ModelObservable
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.screens.habits.list.HabitCardListCache
import io.github.crisacm.salem.core.utils.MidnightTimer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HabitListStateTest : BaseAndroidJVMTest() {
    private val midnightTimer = mock<MidnightTimer>()
    private lateinit var cache: HabitCardListCache
    private lateinit var state: HabitListState
    private lateinit var ids: List<Long>
    private var notifications = 0

    @Before
    override fun setUp() {
        super.setUp()
        habitList.removeAll()
        for (i in 0..2) habitList.add(fixtures.createShortHabit())
        ids = habitList.map { it.id!! }
        cache = HabitCardListCache(habitList, commandRunner, taskRunner, mock())
        state = HabitListState(cache, Preferences(MemoryStorage()), midnightTimer)
        state.observable.addListener(
            object : ModelObservable.Listener {
                override fun onModelChange() {
                    notifications++
                }
            }
        )
        state.onAttached()
        notifications = 0
    }

    @Test
    fun attachingBuildsTheRowsFromTheCache() {
        assertEquals(3, state.itemCount)
        assertEquals(ids, state.rows.map { it.habit.id })
        val row = state.rows[0]
        assertEquals(cache.getScore(ids[0]), row.score, 0.0)
        assertEquals(cache.getCheckmarks(ids[0]).toList(), row.checkmarks.toList())
    }

    @Test
    fun deletingAHabitRemovesItsRow() {
        val habit = habitList.getById(ids[1])!!
        commandRunner.run(DeleteHabitsCommand(habitList, listOf(habit)))
        assertEquals(listOf(ids[0], ids[2]), state.rows.map { it.habit.id })
        assertTrue(notifications > 0)
    }

    @Test
    fun reorderingShowsTheNewOrder() {
        state.performReorder(0, 2)
        assertEquals(listOf(ids[1], ids[2], ids[0]), state.rows.map { it.habit.id })
    }

    @Test
    fun selectionKeepsTheOrderAndDropsUnknownIds() {
        state.select(listOf(ids[2], ids[0], 9999))
        assertEquals(listOf(ids[2], ids[0]), state.selectedIds)
        assertEquals(listOf(ids[2], ids[0]), state.getSelected().map { it.id })
        assertFalse(state.isSelectionEmpty)
    }

    @Test
    fun theSelectionSurvivesTheRemovalOfItsRows() {
        state.select(listOf(ids[1]))
        state.performRemove(state.getSelected())
        assertEquals(2, state.itemCount)
        assertEquals(listOf(ids[1]), state.getSelected().map { it.id })
    }

    @Test
    fun clearingTheSelectionNotifiesOnlyWhenThereWasOne() {
        state.clearSelection()
        assertEquals(0, notifications)

        state.select(listOf(ids[0]))
        state.clearSelection()
        assertTrue(state.isSelectionEmpty)
        assertEquals(emptyList<Long>(), state.selectedIds)
        assertEquals(1, notifications)
    }

    @Test
    fun reorderingIsOnlyAllowedByPosition() {
        assertTrue(state.isSortable)
        state.primaryOrder = HabitList.Order.BY_NAME_ASC
        assertFalse(state.isSortable)
    }

    @Test
    fun attachingAndDetachingRegisterTheMidnightListener() {
        verify { midnightTimer.addListener(state) }
        state.onDetached()
        verify { midnightTimer.removeListener(state) }
    }

    @Test
    fun aRefreshInTheMiddleOfADragWaitsForTheEndOfTheDrag() {
        state.beginDrag()
        val habit = habitList.getById(ids[1])!!
        commandRunner.run(DeleteHabitsCommand(habitList, listOf(habit)))
        assertEquals(3, state.itemCount)
        state.endDrag(null)
        assertEquals(listOf(ids[0], ids[2]), state.rows.map { it.habit.id })
    }

    @Test
    fun theOwnMovesOfADragShowRightAway() {
        state.beginDrag()
        state.performReorder(0, 2)
        assertEquals(listOf(ids[1], ids[2], ids[0]), state.rows.map { it.habit.id })
    }

    @Test
    fun aRefreshThatLandedDuringADragIsReappliedWithTheNetMove() {
        state.beginDrag()
        state.performReorder(0, 2)
        cache.refreshAllHabits()
        assertEquals(listOf(ids[1], ids[2], ids[0]), state.rows.map { it.habit.id })
        state.endDrag(0 to 2)
        assertEquals(listOf(ids[1], ids[2], ids[0]), state.rows.map { it.habit.id })
    }

    @Test
    fun theOldStateLeavingDoesNotStopTheNewOneFromSeeingCommands() {
        val newer = HabitListState(cache, Preferences(MemoryStorage()), midnightTimer)
        newer.onAttached()
        state.onDetached()
        val habit = habitList.getById(ids[1])!!
        commandRunner.run(DeleteHabitsCommand(habitList, listOf(habit)))
        assertEquals(listOf(ids[0], ids[2]), newer.rows.map { it.habit.id })
    }
}
