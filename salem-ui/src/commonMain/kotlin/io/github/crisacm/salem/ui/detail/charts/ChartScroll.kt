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

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * The horizontal scroll of a chart that draws columns of data (see `ChartWindow.kt`). The chart tells it how many
 * columns it has and how many fit in its width; the card reads [visible] to name the period that is in view.
 */
@Stable
class ChartScroll {
    private var moved by mutableFloatStateOf(0f)

    /** The width in pixels of the part of the chart that has data; the chart sets it when it gets its size. */
    var viewportPx by mutableFloatStateOf(0f)

    private var count = 0
    private var columns = 1

    /** Says how many columns of data there are and how many of them fit in the chart. */
    fun bind(count: Int, columns: Int) {
        this.count = count
        this.columns = columns
    }

    val columnPx: Float
        get() = if (viewportPx > 0f && columns > 0) viewportPx / columns else 0f

    private val maxOffsetPx: Float
        get() = maxChartOffset(count, columnPx, viewportPx)

    /** How far the data has moved to the right. */
    val offsetPx: Float
        get() = moved.coerceIn(0f, maxOffsetPx)

    /** The columns that are in view; before the chart has a size, the newest ones that fit. */
    fun visible(): IntRange =
        if (columnPx <= 0f) 0 until minOf(count, columns) else visibleColumns(count, columnPx, viewportPx, offsetPx)

    internal fun consume(delta: Float): Float {
        val target = (offsetPx + delta).coerceIn(0f, maxOffsetPx)
        val used = target - offsetPx
        moved = target
        return used
    }
}

/** A scroll that starts again at the newest data whenever [key] changes. */
@Composable
fun rememberChartScroll(key: Any? = null): ChartScroll = remember(key) { ChartScroll() }

/** Lets a drag and a fling move the data of a chart. */
@Composable
fun Modifier.chartScrollable(scroll: ChartScroll): Modifier {
    val state = rememberScrollableState { scroll.consume(it) }
    return scrollable(state, Orientation.Horizontal)
}
