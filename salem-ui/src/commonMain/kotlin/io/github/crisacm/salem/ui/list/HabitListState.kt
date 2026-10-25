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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.HabitMatcher
import io.github.crisacm.salem.core.models.ModelObservable
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.screens.habits.list.HabitCardListCache
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsMenuBehavior
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior
import io.github.crisacm.salem.core.utils.MidnightTimer

/** The most days a row can show: the header never asks for more columns than this. */
const val MAX_CHECKMARK_COUNT = 60

/** What a row of the list shows, copied from the cache when it changes. */
class HabitRowData(
    val habit: Habit,
    val score: Double,
    val checkmarks: IntArray,
    val completed: BooleanArray,
    val notes: Array<String>
)

/**
 * Data of the habit list for Compose. The habits, scores and checkmarks come
 * from a [HabitCardListCache]; this class turns its index-based callbacks into
 * a list of [HabitRowData] and holds the selected rows.
 */
class HabitListState(
    private val cache: HabitCardListCache,
    private val preferences: Preferences,
    private val midnightTimer: MidnightTimer
) : HabitCardListCache.Listener,
    MidnightTimer.MidnightListener,
    ListHabitsMenuBehavior.Adapter,
    ListHabitsSelectionMenuBehavior.Adapter {
    val observable: ModelObservable = ModelObservable()

    var rows: List<HabitRowData> by mutableStateOf(emptyList())
        private set

    /** Ids of the selected habits, in order of selection. */
    var selectedIds: List<Long> by mutableStateOf(emptyList())
        private set

    // Kept apart from the rows because a deleted habit leaves the rows before the
    // delete command asks for the selection again.
    private var selectedHabits: List<Habit> = emptyList()

    private var dragging = false
    private var refreshWaiting = false
    private var ownMove = false

    val itemCount: Int
        get() = rows.size

    val isSelectionEmpty: Boolean
        get() = selectedHabits.isEmpty()

    val isSortable: Boolean
        get() = cache.primaryOrder == HabitList.Order.BY_POSITION

    init {
        cache.setListener(this)
        cache.setCheckmarkCount(MAX_CHECKMARK_COUNT)
        cache.secondaryOrder = preferences.defaultSecondaryOrder
        cache.primaryOrder = preferences.defaultPrimaryOrder
    }

    fun onAttached() {
        cache.onAttached()
        midnightTimer.addListener(this)
    }

    fun onDetached() {
        // The cache is shared by every activity: a screen that is replaced by a newer one must not
        // detach the cache the newer one just attached.
        if (cache.isListener(this)) cache.onDetached()
        midnightTimer.removeListener(this)
    }

    fun cancelRefresh() = cache.cancelTasks()

    fun hasNoHabit(): Boolean = cache.hasNoHabit()

    override fun atMidnight() = cache.refreshAllHabits()

    override fun refresh() = cache.refreshAllHabits()

    override fun setFilter(matcher: HabitMatcher) = cache.setFilter(matcher)

    override var primaryOrder: HabitList.Order
        get() = cache.primaryOrder
        set(value) {
            cache.primaryOrder = value
            preferences.defaultPrimaryOrder = value
        }

    override var secondaryOrder: HabitList.Order
        get() = cache.secondaryOrder
        set(value) {
            cache.secondaryOrder = value
            preferences.defaultSecondaryOrder = value
        }

    /** Replaces the selection by the rows with these ids; unknown ids are dropped. */
    fun select(ids: List<Long>) {
        val known = (selectedHabits + rows.map { it.habit }).associateBy { it.id }
        selectedHabits = ids.mapNotNull { known[it] }
        selectedIds = selectedHabits.map { it.id!! }
    }

    override fun clearSelection() {
        if (selectedHabits.isEmpty()) return
        selectedHabits = emptyList()
        selectedIds = emptyList()
        observable.notifyListeners()
    }

    override fun getSelected(): List<Habit> = ArrayList(selectedHabits)

    /** Only changes the cache; the database is touched by the command that follows. */
    override fun performRemove(selected: List<Habit>) {
        for (habit in selected) cache.remove(habit.id!!)
    }

    /** Only changes the cache; the database is touched by the behavior. */
    fun performReorder(from: Int, to: Int) = moveInCache(from, to)

    /** While a row is dragged the rows only change with the moves of the drag itself. */
    fun beginDrag() {
        dragging = true
    }

    /**
     * Ends the drag and shows what the cache did meanwhile. When a refresh arrived during the drag the
     * cache holds the order of the database, which does not have the drag yet, so [move] (the net move
     * of the drag) is applied to it again.
     */
    fun endDrag(move: Pair<Int, Int>?) {
        dragging = false
        if (!refreshWaiting) return
        refreshWaiting = false
        if (move != null && move.first in 0 until cache.habitCount && move.second in 0 until cache.habitCount) {
            moveInCache(move.first, move.second)
        } else {
            changed()
        }
    }

    private fun moveInCache(from: Int, to: Int) {
        ownMove = true
        try {
            cache.reorder(from, to)
        } finally {
            ownMove = false
        }
    }

    override fun onItemChanged(position: Int) = changed()

    override fun onItemInserted(position: Int) = changed()

    override fun onItemMoved(oldPosition: Int, newPosition: Int) = changed()

    override fun onItemRemoved(position: Int) = changed()

    override fun onRefreshFinished() = changed()

    private fun changed() {
        if (dragging && !ownMove) {
            refreshWaiting = true
            return
        }
        rows = buildRows()
        observable.notifyListeners()
    }

    private fun buildRows(): List<HabitRowData> =
        (0 until cache.habitCount).mapNotNull { cache.getHabitByPosition(it) }.map { habit ->
            val id = habit.id!!
            HabitRowData(
                habit,
                cache.getScore(id),
                cache.getCheckmarks(id),
                cache.getCompleted(id),
                cache.getNotes(id)
            )
        }
}
