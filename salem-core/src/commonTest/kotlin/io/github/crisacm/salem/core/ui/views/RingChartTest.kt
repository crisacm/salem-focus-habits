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

package io.github.crisacm.salem.core.ui.views

import io.github.crisacm.salem.platform.gui.Color
import io.github.crisacm.salem.platform.gui.FontAwesome
import io.github.crisacm.salem.platform.gui.assertRenders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class RingChartTest {
    private val base = "views/RingChart"
    private val green = LightTheme().color(7)

    @Test
    fun testDrawWithAColourHole() = runTest {
        val ring = RingChart(green, 0.6, 8.0, Color.BLACK.withAlpha(0.15), holeColor = Color.WHITE, text = "60%")
        assertRenders(100, 100, "$base/colourHole.png", ring)
    }

    @Test
    fun testDrawWithATransparentHole() = runTest {
        val ring = RingChart(green, 0.6, 8.0, Color.WHITE.withAlpha(0.15), holeColor = null, text = "60%")
        assertRenders(100, 100, "$base/transparentHole.png", ring)
    }

    @Test
    fun testDrawAQuarter() = runTest {
        val ring = RingChart(green, 0.25, 5.0, Color.BLACK.withAlpha(0.15), holeColor = Color.WHITE, text = "25%")
        assertRenders(100, 100, "$base/quarter.png", ring)
    }

    @Test
    fun testDrawFontAwesomeText() = runTest {
        val ring = RingChart(
            green,
            0.9,
            6.0,
            Color.WHITE.withAlpha(0.15),
            holeColor = null,
            text = FontAwesome.CHECK,
            isFontAwesome = true,
            textSize = 28.0
        )
        assertRenders(100, 100, "$base/checkmark.png", ring)
    }

    @Test
    fun testDrawStrokedFontAwesomeText() = runTest {
        val ring = RingChart(
            green,
            0.9,
            6.0,
            Color.WHITE.withAlpha(0.15),
            holeColor = null,
            text = FontAwesome.CHECK,
            isFontAwesome = true,
            textSize = 28.0,
            isTextStroked = true
        )
        assertRenders(100, 100, "$base/strokedCheckmark.png", ring)
    }

    @Test
    fun testDrawFullAndEmpty() = runTest {
        assertRenders(
            100,
            100,
            "$base/full.png",
            RingChart(green, 1.0, 8.0, Color.BLACK.withAlpha(0.15), holeColor = Color.WHITE, text = "100%")
        )
        assertRenders(
            100,
            100,
            "$base/empty.png",
            RingChart(green, 0.0, 8.0, Color.BLACK.withAlpha(0.15), holeColor = Color.WHITE, text = "0%")
        )
    }
}
