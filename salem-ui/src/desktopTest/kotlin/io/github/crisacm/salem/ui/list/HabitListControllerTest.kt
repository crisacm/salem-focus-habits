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

import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifyNoMoreCalls
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.screens.habits.list.HabitCardListCache
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior
import io.github.crisacm.salem.core.utils.MidnightTimer
import io.github.crisacm.salem.ui.BaseUiTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HabitListControllerTest : BaseUiTest() {
    private val behavior = mock<ListHabitsBehavior>()
    private val selection = mock<SelectionListener>()
    private lateinit var state: HabitListState
    private lateinit var controller: HabitListController
    private lateinit var ids: List<Long>
    private lateinit var cache: HabitCardListCache

    @Before
    override fun setUp() {
        super.setUp()
        habitList.removeAll()
        for (i in 0..2) habitList.add(fixtures.createShortHabit())
        ids = habitList.map { it.id!! }
        cache = HabitCardListCache(habitList, commandRunner, taskRunner, mock())
        state = HabitListState(cache, Preferences(MemoryStorage()), mock<MidnightTimer>())
        state.onAttached()
        controller = HabitListController(state, behavior, lazyOf(selection))
    }

    private fun habit(index: Int) = state.rows[index].habit

    private fun cacheRefresh() = cache.refreshAllHabits()

    @Test
    fun clickWithoutSelectionOpensTheHabit() {
        controller.onClick(habit(1))
        verify { behavior.onClickHabit(habit(1)) }
        assertTrue(state.isSelectionEmpty)
    }

    @Test
    fun longPressStartsTheSelection() {
        controller.onLongClick(habit(1))
        assertEquals(listOf(ids[1]), state.selectedIds)
        verify { selection.onSelectionStart() }
    }

    @Test
    fun clickWithSelectionTogglesRowsUntilItIsEmpty() {
        controller.onLongClick(habit(0))
        controller.onClick(habit(2))
        assertEquals(listOf(ids[0], ids[2]), state.selectedIds)
        verify { selection.onSelectionChange() }

        controller.onClick(habit(0))
        controller.onClick(habit(2))
        assertTrue(state.isSelectionEmpty)
        verify { selection.onSelectionFinish() }
        verify(VerifyMode.exactly(0)) { behavior.onClickHabit(habit(0)) }
    }

    @Test
    fun dropMovesTheRowOnScreenAndCancelsTheSelectionButDoesNotPersistYet() {
        controller.onLongClick(habit(0))
        controller.drop(0, 2)
        assertEquals(listOf(ids[1], ids[2], ids[0]), state.rows.map { it.habit.id })
        assertTrue(state.isSelectionEmpty)
        verify { selection.onSelectionFinish() }
        verify(VerifyMode.exactly(0)) { behavior.onReorderHabit(habit(0), habit(0)) }
        verifyNoMoreCalls(behavior)
    }

    @Test
    fun aDragWithSeveralStepsIsPersistedOnceWithTheNetMove() {
        val start = state.rows.map { it.habit }
        controller.onDragStarted(start[0])
        controller.drop(0, 1)
        controller.drop(1, 2)
        controller.onDragFinished()
        assertEquals(listOf(ids[1], ids[2], ids[0]), state.rows.map { it.habit.id })
        verify(VerifyMode.exactly(1)) { behavior.onReorderHabit(start[0], start[2]) }
    }

    @Test
    fun aDragThatComesBackWhereItStartedPersistsNothing() {
        val start = state.rows.map { it.habit }
        controller.onDragStarted(start[1])
        controller.drop(1, 2)
        controller.drop(2, 1)
        controller.onDragFinished()
        assertEquals(ids, state.rows.map { it.habit.id })
        verifyNoMoreCalls(behavior)
    }

    @Test
    fun aDragWithoutMovesPersistsNothing() {
        controller.onDragStarted(habit(0))
        controller.onDragFinished()
        controller.onDragFinished()
        verifyNoMoreCalls(behavior)
    }

    @Test
    fun aRefreshInTheMiddleOfADragDoesNotMoveTheRowsAndTheDropIsKept() {
        val start = state.rows.map { it.habit }
        controller.onDragStarted(start[0])
        controller.drop(0, 2)
        cacheRefresh()
        assertEquals(listOf(ids[1], ids[2], ids[0]), state.rows.map { it.habit.id })
        controller.onDragFinished()
        assertEquals(listOf(ids[1], ids[2], ids[0]), state.rows.map { it.habit.id })
        verify { behavior.onReorderHabit(start[0], start[2]) }
    }

    @Test
    fun finishingFromTheBarClearsTheSelection() {
        controller.onLongClick(habit(0))
        controller.onSelectionFinished()
        assertTrue(state.isSelectionEmpty)
        verify { selection.onSelectionFinish() }
    }
}
