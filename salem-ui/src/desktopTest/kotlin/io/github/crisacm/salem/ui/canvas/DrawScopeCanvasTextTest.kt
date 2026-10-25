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

import io.github.crisacm.salem.platform.gui.Color
import io.github.crisacm.salem.platform.gui.Font
import io.github.crisacm.salem.platform.gui.TextAlign
import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Where [DrawScopeCanvas] puts text. Like `AndroidCanvas`, the `y` of a line is about the middle of its lower-case
 * letters, so its baseline is 0.6 times the height of an "m" below `y`; the charts of the core are laid out for that.
 */
class DrawScopeCanvasTextTest {
    private val black = Color(0.0, 0.0, 0.0, 1.0)

    // A large font makes the difference between the placements several pixels wide
    private val size = 40.0
    private val y = 40.0

    /** The last row, in pixels, with ink: the baseline of a text without descenders. */
    private fun BufferedImage.inkBottom() =
        (height - 1 downTo 0).first { row -> (0 until width).any { (getRGB(it, row) ushr 24) != 0 } }

    private fun assertNear(expected: Int, actual: Int, tolerance: Int) =
        assertTrue(kotlin.math.abs(expected - actual) <= tolerance, "$actual is not within $tolerance of $expected")

    private fun bottomOf(text: String, font: Font = Font.REGULAR, fontSize: Double = size): Int =
        render(160, 110) {
            it.setColor(black)
            it.setFont(font)
            it.setFontSize(fontSize)
            it.setTextAlign(TextAlign.LEFT)
            it.drawText(text, 10.0, y)
        }.inkBottom()

    @Test
    fun theBaselineIsSixTenthsOfTheHeightOfAnMBelowY() {
        // 2 pixels per dp; the "m" of Noto Sans is about 0.54 times the font size tall
        val expected = (y + 0.6 * 0.54 * size) * 2
        assertEquals(expected, bottomOf("xxx").toDouble(), 1.5)
    }

    @Test
    fun textsOfDifferentHeightShareTheBaseline() {
        assertNear(bottomOf("xxx"), bottomOf("HHx"), 1)
        assertNear(bottomOf("xxx"), bottomOf("0123"), 1)
    }

    @Test
    fun boldTextSitsOnTheSameBaseline() {
        assertNear(bottomOf("xxx"), bottomOf("xxx", Font.BOLD), 1)
    }

    @Test
    fun theBaselineFollowsTheFontSize() {
        val small = bottomOf("xxx", fontSize = 20.0)
        val big = bottomOf("xxx", fontSize = 40.0)
        assertEquals((y + 0.6 * 0.54 * 20) * 2, small.toDouble(), 1.5)
        assertTrue(big > small, "a bigger font puts the baseline lower: $small, $big")
    }

    @Test
    fun strokedTextUsesTheSameBaseline() {
        val image = render(160, 110) {
            it.setColor(black)
            it.setFontSize(size)
            it.setStrokeWidth(1.0)
            it.setTextAlign(TextAlign.LEFT)
            it.strokeText("xxx", 10.0, y)
        }
        assertNear(bottomOf("xxx"), image.inkBottom(), 3)
    }

    @Test
    fun measuresWithTheCurrentFontAndSize() {
        render(10, 10) {
            it.setFontSize(14.0)
            val small = it.measureText("MMMM")
            it.setFontSize(28.0)
            assertEquals(2 * small, it.measureText("MMMM"), 0.05 * small)
            it.setFont(Font.BOLD)
            assertTrue(it.measureText("MMMM") > 2 * small, "bold is wider than regular")
        }
    }

    @Test
    fun theLineSpacingFollowsTheFontSize() {
        render(10, 10) {
            it.setFontSize(14.0)
            val small = it.fontSpacing()
            it.setFontSize(28.0)
            assertEquals(2 * small, it.fontSpacing(), 0.1 * small)
        }
    }
}
