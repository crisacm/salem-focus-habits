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

import io.github.crisacm.salem.platform.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class HeaderModelTest {
    private val today = LocalDate(5000)

    @Test
    fun theScrollDirectionIsFlippedByReversedAndByRtl() {
        assertEquals(-1, scrollDirection(reversed = false, rtl = false))
        assertEquals(1, scrollDirection(reversed = true, rtl = false))
        assertEquals(1, scrollDirection(reversed = false, rtl = true))
        assertEquals(-1, scrollDirection(reversed = true, rtl = true))
    }

    @Test
    fun todayIsFirstWhenTheOrderIsNotReversed() {
        val dates = headerColumns(count = 3, dataOffset = 0, today = today, reversed = false)
        assertEquals(listOf(today, today.minus(1), today.minus(2)), dates)
    }

    @Test
    fun todayIsLastWhenTheOrderIsReversed() {
        val dates = headerColumns(count = 3, dataOffset = 0, today = today, reversed = true)
        assertEquals(listOf(today.minus(2), today.minus(1), today), dates)
    }

    @Test
    fun theOffsetMovesEveryColumnIntoThePast() {
        val dates = headerColumns(count = 3, dataOffset = 4, today = today, reversed = false)
        assertEquals(listOf(today.minus(4), today.minus(5), today.minus(6)), dates)
    }

    @Test
    fun withoutColumnsThereAreNoDates() {
        assertEquals(emptyList<LocalDate>(), headerColumns(count = 0, dataOffset = 2, today = today, reversed = false))
    }

    @Test
    fun draggingConvertsPixelsIntoDays() {
        val scroll = HeaderScroll(bucketPx = 100f, maxOffset = 10)
        scroll.dragBy(delta = -250f, direction = -1)
        assertEquals(2, scroll.dataOffset)
        scroll.dragBy(delta = -60f, direction = -1)
        assertEquals(3, scroll.dataOffset)
    }

    @Test
    fun draggingTheOtherWayGoesBackToToday() {
        val scroll = HeaderScroll(bucketPx = 100f, maxOffset = 10)
        scroll.setOffset(5)
        scroll.dragBy(delta = 200f, direction = -1)
        assertEquals(3, scroll.dataOffset)
    }

    @Test
    fun theDirectionFlipsTheDrag() {
        val scroll = HeaderScroll(bucketPx = 100f, maxOffset = 10)
        scroll.dragBy(delta = 300f, direction = 1)
        assertEquals(3, scroll.dataOffset)
    }

    @Test
    fun theOffsetNeverGoesBelowZeroOrAboveTheMaximum() {
        val scroll = HeaderScroll(bucketPx = 100f, maxOffset = 4)
        scroll.dragBy(delta = 5000f, direction = -1)
        assertEquals(0, scroll.dataOffset)
        scroll.dragBy(delta = -5000f, direction = -1)
        assertEquals(4, scroll.dataOffset)
    }

    @Test
    fun thereIsNoDeadZoneAfterDraggingPastTheStart() {
        val scroll = HeaderScroll(bucketPx = 100f, maxOffset = 4)
        scroll.dragBy(delta = 5000f, direction = -1)
        scroll.dragBy(delta = -100f, direction = -1)
        assertEquals(1, scroll.dataOffset)
    }

    @Test
    fun loweringTheMaximumClampsTheOffset() {
        val scroll = HeaderScroll(bucketPx = 100f, maxOffset = 10)
        scroll.setOffset(8)
        scroll.updateMax(5)
        assertEquals(5, scroll.dataOffset)
        scroll.updateMax(0)
        assertEquals(0, scroll.dataOffset)
    }

    @Test
    fun settingTheOffsetIsClamped() {
        val scroll = HeaderScroll(bucketPx = 100f, maxOffset = 3)
        scroll.setOffset(9)
        assertEquals(3, scroll.dataOffset)
        scroll.setOffset(-2)
        assertEquals(0, scroll.dataOffset)
    }
}
