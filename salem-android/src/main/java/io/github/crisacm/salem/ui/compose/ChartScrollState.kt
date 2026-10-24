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

package io.github.crisacm.salem.ui.compose

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import io.github.crisacm.salem.activities.habits.list.HeaderScroll

// The old charts let the data scroll without an end; this is far more columns than any habit has.
private const val MAX_CHART_OFFSET = 100_000

/**
 * The horizontal scroll of a chart that draws columns of data (the history and the bars): [dataOffset] is how
 * many columns the data is moved to the right, so older values come into view. It never goes below zero.
 */
class ChartScrollState {
    var dataOffset by mutableIntStateOf(0)
        private set

    private val scroll = HeaderScroll(bucketPx = 0f, maxOffset = MAX_CHART_OFFSET)
    private var gesture = 0

    /** The width in pixels of one column; the chart knows it only after it has drawn once. */
    fun setColumnWidth(px: Float) {
        if (scroll.bucketPx == px) return
        val offset = scroll.dataOffset
        scroll.bucketPx = px
        scroll.setOffset(offset)
        publish()
    }

    fun dragBy(deltaPx: Float) {
        gesture++
        scroll.dragBy(deltaPx, 1)
        publish()
    }

    fun reset() {
        gesture++
        scroll.moveTo(0f)
        publish()
    }

    /** Keeps scrolling after a drag, slowing down until it stops or reaches an end; a new drag stops it. */
    suspend fun fling(velocityPxPerSecond: Float) {
        val id = ++gesture
        val animation = AnimationState(initialValue = scroll.x, initialVelocity = velocityPxPerSecond)
        animation.animateDecay(exponentialDecay()) {
            if (gesture != id) {
                cancelAnimation()
                return@animateDecay
            }
            scroll.moveTo(value)
            publish()
            if (scroll.x != value) cancelAnimation()
        }
    }

    private fun publish() {
        val offset = scroll.dataOffset
        if (offset != dataOffset) dataOffset = offset
    }
}
