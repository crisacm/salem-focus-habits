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

package io.github.crisacm.salem.ui.detail.charts

import io.github.crisacm.salem.platform.time.LocalDate
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/*
 * The charts of the detail screen draw columns of data, newest first: column 0 is the newest and sits at the
 * right end. Dragging moves older columns into view, so the offset is the distance in pixels that the data has
 * moved to the right and never goes below zero.
 */

/** How far the data can move to the right: until the oldest column reaches the left end of the chart. */
fun maxChartOffset(count: Int, columnPx: Float, viewportPx: Float): Float =
    max(0f, count * columnPx - viewportPx)

/** The columns that are at least partly inside a viewport of [viewportPx] pixels when the data moved [offsetPx]. */
fun visibleColumns(count: Int, columnPx: Float, viewportPx: Float, offsetPx: Float): IntRange {
    if (count <= 0 || columnPx <= 0f) return IntRange.EMPTY
    val first = floor(offsetPx / columnPx).toInt().coerceIn(0, count - 1)
    val last = (ceil((offsetPx + viewportPx) / columnPx).toInt() - 1).coerceIn(first, count - 1)
    return first..last
}

/** The x of the left edge of column [index] in the viewport. */
fun columnLeft(index: Int, columnPx: Float, viewportPx: Float, offsetPx: Float): Float =
    viewportPx - (index + 1) * columnPx + offsetPx

/** How many columns of a chart of the score fit in the screen for a bucket of [bucketSize] days. */
fun scoreColumnsInView(bucketSize: Int): Int = when (bucketSize) {
    1 -> 28
    7 -> 16
    31 -> 12
    92 -> 8
    else -> 6
}

/** How many bars fit in the screen for a bucket of [bucketSize] days. */
fun barColumnsInView(bucketSize: Int): Int = when (bucketSize) {
    92 -> 8
    365 -> 6
    else -> 14
}

/** How many weeks of the calendar fit in the screen. */
const val CALENDAR_COLUMNS_IN_VIEW = 20

/**
 * The period shown, as the subtitle of a card: "Apr 2021", "Jan – Apr 2021" or "Mar 2020 – Apr 2021".
 * [monthName] gives the short name of the month of a date.
 */
fun rangeLabel(oldest: LocalDate, newest: LocalDate, monthName: (LocalDate) -> String): String = when {
    oldest.year == newest.year && oldest.month == newest.month -> "${monthName(newest)} ${newest.year}"
    oldest.year == newest.year -> "${monthName(oldest)} \u2013 ${monthName(newest)} ${newest.year}"
    else -> "${monthName(oldest)} ${oldest.year} \u2013 ${monthName(newest)} ${newest.year}"
}
