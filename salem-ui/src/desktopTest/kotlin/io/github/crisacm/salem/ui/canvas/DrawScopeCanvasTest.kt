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

package io.github.crisacm.salem.ui.canvas

import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.ui.views.RingChart
import io.github.crisacm.salem.platform.gui.Color
import io.github.crisacm.salem.platform.gui.FontAwesome
import kotlin.test.Test

class DrawScopeCanvasTest {
    private val green = LightTheme().color(7)

    @Test
    fun ringWithAColourHole() {
        val ring = RingChart(green, 0.6, 8.0, Color.BLACK.withAlpha(0.15), holeColor = Color.WHITE, text = "60%")
        assertRendersLikeCore(100, 100, "views/RingChart/colourHole.png", ring)
    }

    @Test
    fun ringQuarter() {
        val ring = RingChart(green, 0.25, 5.0, Color.BLACK.withAlpha(0.15), holeColor = Color.WHITE, text = "25%")
        assertRendersLikeCore(100, 100, "views/RingChart/quarter.png", ring)
    }

    @Test
    fun ringWithATransparentHoleAndFontAwesome() {
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
        assertRendersLikeCore(100, 100, "views/RingChart/checkmark.png", ring)
    }

    @Test
    fun ringWithStrokedFontAwesome() {
        val ring = RingChart(
            green, 0.9, 6.0, Color.WHITE.withAlpha(0.15), holeColor = null,
            text = FontAwesome.CHECK, isFontAwesome = true, textSize = 28.0, isTextStroked = true
        )
        assertRendersLikeCore(100, 100, "views/RingChart/strokedCheckmark.png", ring)
    }
}
