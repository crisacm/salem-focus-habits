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

import androidx.compose.ui.text.font.FontFamily
import io.github.crisacm.salem.platform.gui.Color
import io.github.crisacm.salem.platform.gui.Font
import io.github.crisacm.salem.platform.gui.Image
import io.github.crisacm.salem.platform.gui.TextAlign
import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** The primitives of [DrawScopeCanvas], checked on pixels: the bitmap is 2 pixels per dp. */
class DrawScopeCanvasPrimitivesTest {
    private val red = Color(1.0, 0.0, 0.0, 1.0)

    private fun BufferedImage.alpha(dpX: Int, dpY: Int) = (getRGB(dpX * 2, dpY * 2) ushr 24) and 0xFF
    private fun BufferedImage.rgb(dpX: Int, dpY: Int) = getRGB(dpX * 2, dpY * 2) and 0xFFFFFF

    /** The columns, in pixels, between the first and the last one with ink. */
    private fun BufferedImage.inkColumns(): IntRange {
        val columns = (0 until width).filter { x -> (0 until height).any { y -> (getRGB(x, y) ushr 24) != 0 } }
        return columns.first()..columns.last()
    }

    private fun BufferedImage.inkPixels() = (0 until width).sumOf { x -> (0 until height).count { y -> (getRGB(x, y) ushr 24) != 0 } }

    @Test
    fun reportsItsSizeInDp() {
        render(100, 60) {
            assertEquals(100.0, it.getWidth())
            assertEquals(60.0, it.getHeight())
        }
    }

    @Test
    fun fillsARectangle() {
        val image = render(40, 40) {
            it.setColor(red)
            it.fillRect(10.0, 10.0, 20.0, 20.0)
        }
        assertEquals(0xFF0000, image.rgb(20, 20))
        assertEquals(255, image.alpha(20, 20))
        assertEquals(0, image.alpha(5, 5))
        assertEquals(0, image.alpha(35, 35))
    }

    @Test
    fun fillsARoundedRectangleWithoutItsCorners() {
        val image = render(40, 40) {
            it.setColor(red)
            it.fillRoundRect(0.0, 0.0, 40.0, 40.0, 15.0)
        }
        assertEquals(255, image.alpha(20, 20))
        assertEquals(0, image.alpha(0, 0))
    }

    @Test
    fun drawsOnlyTheOutlineOfARectangle() {
        val image = render(40, 40) {
            it.setColor(red)
            it.setStrokeWidth(2.0)
            it.drawRect(5.0, 5.0, 30.0, 30.0)
        }
        assertTrue(image.alpha(5, 20) > 0)
        assertEquals(0, image.alpha(20, 20))
    }

    @Test
    fun drawsALine() {
        val image = render(40, 40) {
            it.setColor(red)
            it.setStrokeWidth(2.0)
            it.drawLine(0.0, 20.0, 40.0, 20.0)
        }
        assertEquals(255, image.alpha(20, 20))
        assertEquals(0, image.alpha(20, 10))
    }

    @Test
    fun keepsColourComponentsInRange() {
        val image = render(10, 10) {
            it.setColor(Color(2.0, -1.0, 0.0, 1.5))
            it.fill()
        }
        assertEquals(0xFF0000, image.rgb(5, 5))
        assertEquals(255, image.alpha(5, 5))
    }

    @Test
    fun cutsAHoleWithClearCircle() {
        val image = render(40, 40) {
            it.setColor(red)
            it.fill()
            it.clearCircle(20.0, 20.0, 10.0)
        }
        assertEquals(0, image.alpha(20, 20))
        assertEquals(255, image.alpha(2, 2))
    }

    @Test
    fun alignsTextToTheLeftAndToTheRightOfX() {
        fun ink(align: TextAlign) = render(100, 30) {
            it.setColor(red)
            it.setFontSize(14.0)
            it.setTextAlign(align)
            it.drawText("Salem", 50.0, 15.0)
        }.inkColumns()

        val left = ink(TextAlign.LEFT)
        val center = ink(TextAlign.CENTER)
        val right = ink(TextAlign.RIGHT)
        assertTrue(left.first >= 98, "left starts at x: $left")
        assertTrue(right.last <= 102, "right ends at x: $right")
        assertTrue(center.first < 100 && center.last > 100, "centre surrounds x: $center")
    }

    @Test
    fun drawsBoldTextWithMoreInkThanRegular() {
        fun ink(font: Font) = render(100, 30) {
            it.setColor(red)
            it.setFont(font)
            it.setFontSize(14.0)
            it.drawText("Salem", 50.0, 15.0)
        }.inkPixels()

        assertTrue(ink(Font.BOLD) > ink(Font.REGULAR))
    }

    @Test
    fun measuresTextInDp() {
        render(10, 10) {
            it.setFontSize(14.0)
            val one = it.measureText("M")
            val four = it.measureText("MMMM")
            assertTrue(one > 5.0 && one < 20.0, "an M is $one dp wide")
            assertEquals(4 * one, four, 0.05 * four)
            assertTrue(it.fontSpacing() >= 14.0, "the spacing is ${it.fontSpacing()} dp")
        }
    }

    @Test
    fun givesTheImageOnlyWhenItHasAProvider() {
        render(10, 10) {
            assertFailsWith<UnsupportedOperationException> { it.toImage() }
        }
        val image = object : Image {
            override val width = 1
            override val height = 1
            override fun getPixel(x: Int, y: Int) = Color.BLACK
            override fun setPixel(x: Int, y: Int, color: Color) {}
            override suspend fun export(path: String) {}
        }
        val canvas = DrawScopeCanvas(textMeasurerForTest(), FontFamily.Default, image = { image })
        assertNotNull(canvas.toImage())
    }
}
