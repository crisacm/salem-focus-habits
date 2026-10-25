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

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.crisacm.salem.platform.time.getToday

/**
 * State of the day header of the habit list. It owns the horizontal scroll, tells the rows through
 * [onDataOffsetChanged] how many days they are scrolled, and holds what the header draws.
 */
class HeaderState(bucketPx: Float, private val onDataOffsetChanged: (Int) -> Unit) {
    var buttonCount by mutableIntStateOf(0)
    var reversed by mutableStateOf(false)
    var today by mutableStateOf(getToday())
    var dataOffset by mutableIntStateOf(0)
        private set

    private val scroll = HeaderScroll(bucketPx)
    private var gesture = 0
    private var pendingOffset: Int? = null
    private var maxKnown = false

    /**
     * Sets how far the header can scroll, which is known once the screen has a size. An offset restored
     * before that is applied now, and the rows are told so that they end up on the same day as the header.
     */
    fun setMaxDataOffset(max: Int) {
        scroll.updateMax(max)
        maxKnown = true
        val pending = pendingOffset
        if (pending != null) {
            pendingOffset = null
            scroll.setOffset(pending)
            dataOffset = scroll.dataOffset
            onDataOffsetChanged(dataOffset)
        } else {
            publish()
        }
    }

    /** Takes the offset the rows already have (for example the one they restored) without notifying them yet. */
    fun restoreOffset(offset: Int) {
        // With the maximum already known there is nothing to wait for.
        pendingOffset = if (maxKnown) null else offset
        scroll.setOffset(offset)
        dataOffset = scroll.dataOffset
    }

    fun dragBy(delta: Float, direction: Int) {
        gesture++
        scroll.dragBy(delta, direction)
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
        if (offset != dataOffset) {
            dataOffset = offset
            onDataOffsetChanged(offset)
        }
    }
}
