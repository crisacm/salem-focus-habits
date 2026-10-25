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

import io.github.crisacm.salem.core.models.Entry
import kotlin.math.floor

/** What a tap or a long press on a row means for the screen. */
enum class RowEvent { OPEN, SELECTION_START, SELECTION_CHANGE, SELECTION_FINISH }

/** The ids selected (in order of selection) after a gesture, and the event to tell the screen. */
class RowResult(val selected: List<Long>, val event: RowEvent)

/**
 * A tap opens the habit when nothing is selected; otherwise it toggles the row
 * in the selection (same rules as the old `HabitCardListController`).
 */
fun rowTap(selected: List<Long>, id: Long): RowResult {
    if (selected.isEmpty()) return RowResult(selected, RowEvent.OPEN)
    return toggled(selected, id)
}

/** A long press starts the selection, or toggles the row when there is one. */
fun rowLongPress(selected: List<Long>, id: Long): RowResult {
    if (selected.isEmpty()) return RowResult(listOf(id), RowEvent.SELECTION_START)
    return toggled(selected, id)
}

private fun toggled(selected: List<Long>, id: Long): RowResult {
    val next = if (id in selected) selected - id else selected + id
    val event = if (next.isEmpty()) RowEvent.SELECTION_FINISH else RowEvent.SELECTION_CHANGE
    return RowResult(next, event)
}

/**
 * The ids after moving the row at [from] to position [to], as
 * `HabitCardListCache.reorder` does it; null when there is nothing to move.
 */
fun reorderedIds(ids: List<Long>, from: Int, to: Int): List<Long>? {
    if (from == to || from !in ids.indices || to !in ids.indices) return null
    return ids.toMutableList().also { it.add(to, it.removeAt(from)) }
}

/**
 * The index a dragged row would take: the row at [index] has moved [offsetPx]
 * from its place, rows are [rowHeightPx] tall, and the result stays in `[0, count)`.
 */
fun dragTargetIndex(index: Int, offsetPx: Float, rowHeightPx: Float, count: Int): Int {
    if (count <= 0 || rowHeightPx <= 0f) return index
    val centre = (index + 0.5f) * rowHeightPx + offsetPx
    return floor(centre / rowHeightPx).toInt().coerceIn(0, count - 1)
}

/** The checkmark value [daysAgo] days before the first column; unknown past the end of the data. */
fun checkmarkAt(values: IntArray, daysAgo: Int) = values.getOrElse(daysAgo) { Entry.UNKNOWN }

/** The number of a numerical habit [daysAgo] days back (values are stored times 1000); zero past the data. */
fun numberAt(values: IntArray, daysAgo: Int) = (values.getOrNull(daysAgo) ?: 0) / 1000.0

fun noteAt(notes: Array<String>, daysAgo: Int) = notes.getOrElse(daysAgo) { "" }

fun completedAt(completed: BooleanArray, daysAgo: Int) = completed.getOrElse(daysAgo) { false }
