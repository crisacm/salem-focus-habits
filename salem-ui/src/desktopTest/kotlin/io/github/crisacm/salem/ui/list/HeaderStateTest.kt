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

import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.resetToday
import io.github.crisacm.salem.platform.time.setToday
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HeaderStateTest {
    private val notified = mutableListOf<Int>()
    private lateinit var state: HeaderState

    @Before
    fun setUp() {
        setToday(LocalDate(5000))
        state = HeaderState(bucketPx = 100f) { notified += it }
    }

    @After
    fun tearDown() = resetToday()

    @Test
    fun anOffsetRestoredBeforeTheSizeIsKnownSurvives() {
        state.restoreOffset(5)
        state.setMaxDataOffset(56)
        assertEquals(5, state.dataOffset)
        assertEquals(5, notified.last())
    }

    @Test
    fun aRestoredOffsetThatNoLongerFitsIsClampedAndTheRowsAreTold() {
        state.restoreOffset(30)
        state.setMaxDataOffset(10)
        assertEquals(10, state.dataOffset)
        assertEquals(10, notified.last())
    }

    @Test
    fun restoringAloneDoesNotNotifyTheRows() {
        state.restoreOffset(5)
        assertEquals(emptyList<Int>(), notified)
    }

    @Test
    fun withoutARestoredOffsetTheMaximumDoesNotNotify() {
        state.setMaxDataOffset(56)
        assertEquals(emptyList<Int>(), notified)
        assertEquals(0, state.dataOffset)
    }

    @Test
    fun theRestoredOffsetIsOnlyAppliedOnce() {
        state.restoreOffset(5)
        state.setMaxDataOffset(56)
        state.dragBy(delta = -100f, direction = -1)
        state.setMaxDataOffset(56)
        assertEquals(6, state.dataOffset)
    }

    @Test
    fun draggingNotifiesTheRowsOnlyWhenTheDayChanges() {
        state.setMaxDataOffset(10)
        state.dragBy(delta = -30f, direction = -1)
        assertEquals(emptyList<Int>(), notified)
        state.dragBy(delta = -80f, direction = -1)
        assertEquals(listOf(1), notified)
    }

    @Test
    fun anOffsetRestoredAfterTheSizeIsKnownIsNotAppliedAgainLater() {
        state.setMaxDataOffset(56)
        state.restoreOffset(5)
        assertEquals(5, state.dataOffset)
        state.dragBy(delta = -200f, direction = -1)
        state.setMaxDataOffset(56)
        assertEquals(7, state.dataOffset)
    }
}
