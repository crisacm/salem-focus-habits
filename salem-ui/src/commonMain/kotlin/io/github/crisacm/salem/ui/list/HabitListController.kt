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

import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.ModelObservable
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior

/** The bar shown while habits are selected; the list tells it how the selection moves. */
interface SelectionListener {
    fun onSelectionStart()

    fun onSelectionChange()

    fun onSelectionFinish()
}

/**
 * Receives the gestures of the rows: clicks open a habit or change the selection (rules in [rowTap] and
 * [rowLongPress]) and a drag reorders the habits. The selection itself lives in [HabitListState].
 */
class HabitListController(
    private val state: HabitListState,
    private val behavior: ListHabitsBehavior,
    private val selection: Lazy<SelectionListener>
) : ModelObservable.Listener {
    init {
        state.observable.addListener(this)
    }

    fun onClick(habit: Habit) = handle(habit, rowTap(state.selectedIds, habit.id!!))

    fun onLongClick(habit: Habit) = handle(habit, rowLongPress(state.selectedIds, habit.id!!))

    private var draggedHabit: Habit? = null
    private var rowsAtDragStart: List<Habit> = emptyList()

    fun onDragStarted(habit: Habit) {
        draggedHabit = habit
        rowsAtDragStart = state.rows.map { it.habit }
        state.beginDrag()
    }

    /** Moves the row at [from] to [to] on screen; [onDragFinished] saves the whole drag. */
    fun drop(from: Int, to: Int) {
        if (from == to) return
        if (state.rows.getOrNull(from) == null || state.rows.getOrNull(to) == null) return
        cancelSelection()
        state.performReorder(from, to)
    }

    /** Saves the drag as one move, from where the row started to where it ended, if it went anywhere. */
    fun onDragFinished() {
        val habit = draggedHabit ?: return
        draggedHabit = null
        val start = rowsAtDragStart
        val from = start.indexOfFirst { it.id == habit.id }
        val to = state.rows.indexOfFirst { it.habit.id == habit.id }
        val moved = from >= 0 && to in start.indices && from != to
        if (moved) behavior.onReorderHabit(start[from], start[to])
        state.endDrag(if (moved) from to to else null)
    }

    fun onSelectionFinished() = cancelSelection()

    override fun onModelChange() {
        if (state.isSelectionEmpty) selection.value.onSelectionFinish()
    }

    private fun handle(habit: Habit, result: RowResult) {
        if (result.event == RowEvent.OPEN) {
            behavior.onClickHabit(habit)
            return
        }
        state.select(result.selected)
        when (result.event) {
            RowEvent.SELECTION_START -> selection.value.onSelectionStart()
            RowEvent.SELECTION_CHANGE -> selection.value.onSelectionChange()
            else -> selection.value.onSelectionFinish()
        }
    }

    private fun cancelSelection() {
        state.clearSelection()
        selection.value.onSelectionFinish()
    }
}
