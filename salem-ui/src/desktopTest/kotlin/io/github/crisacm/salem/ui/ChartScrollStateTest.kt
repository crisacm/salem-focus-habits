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

import org.junit.Assert.assertEquals
import org.junit.Test

class ChartScrollStateTest {
    @Test
    fun draggingRightMovesOneColumnPerColumnWidth() {
        val scroll = ChartScrollState()
        scroll.setColumnWidth(50f)
        scroll.dragBy(120f)
        assertEquals(2, scroll.dataOffset)
        scroll.dragBy(30f)
        assertEquals(3, scroll.dataOffset)
    }

    @Test
    fun theOffsetNeverGoesBelowZero() {
        val scroll = ChartScrollState()
        scroll.setColumnWidth(50f)
        scroll.dragBy(60f)
        scroll.dragBy(-500f)
        assertEquals(0, scroll.dataOffset)
        // The dead zone of the old scroller does not exist: one column back from zero is not lost.
        scroll.dragBy(50f)
        assertEquals(1, scroll.dataOffset)
    }

    @Test
    fun resetGoesBackToTheFirstColumn() {
        val scroll = ChartScrollState()
        scroll.setColumnWidth(50f)
        scroll.dragBy(200f)
        scroll.reset()
        assertEquals(0, scroll.dataOffset)
    }

    @Test
    fun aNewColumnWidthKeepsTheOffset() {
        val scroll = ChartScrollState()
        scroll.setColumnWidth(50f)
        scroll.dragBy(200f)
        scroll.setColumnWidth(25f)
        assertEquals(4, scroll.dataOffset)
    }

    @Test
    fun withoutAColumnWidthNothingMoves() {
        val scroll = ChartScrollState()
        scroll.dragBy(200f)
        assertEquals(0, scroll.dataOffset)
    }
}
