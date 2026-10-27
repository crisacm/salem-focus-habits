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

package io.github.crisacm.salem.platform.gui

import kotlin.math.roundToInt

/**
 * A [Canvas] that remembers each call so another toolkit (SwiftUI) can paint the same chart.
 * Text is measured as half an em per character: the chart places labels with that width, and the
 * player draws the text at the coordinates the chart already chose.
 */
class RecordingCanvas(
    private val width: Double,
    private val height: Double
) : Canvas {
    val ops = mutableListOf<DrawOp>()
    private var fontSize = 12.0

    override fun setColor(color: Color) {
        ops += DrawOp(DrawOp.SET_COLOR, color = color.toArgb())
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        ops += DrawOp(DrawOp.LINE, x1, y1, x2, y2)
    }

    override fun drawText(text: String, x: Double, y: Double) {
        ops += DrawOp(DrawOp.TEXT, x, y, text = text)
    }

    override fun strokeText(text: String, x: Double, y: Double) {
        ops += DrawOp(DrawOp.TEXT, x, y, text = text, extra = 1)
    }

    override fun fillRect(x: Double, y: Double, width: Double, height: Double) {
        ops += DrawOp(DrawOp.FILL_RECT, x, y, width, height)
    }

    override fun fillRoundRect(x: Double, y: Double, width: Double, height: Double, cornerRadius: Double) {
        ops += DrawOp(DrawOp.FILL_ROUND_RECT, x, y, width, height, cornerRadius)
    }

    override fun drawRect(x: Double, y: Double, width: Double, height: Double) {
        ops += DrawOp(DrawOp.DRAW_RECT, x, y, width, height)
    }

    override fun getHeight() = height

    override fun getWidth() = width

    override fun setFont(font: Font) {
        ops += DrawOp(DrawOp.SET_FONT, extra = font.ordinal)
    }

    override fun setFontSize(size: Double) {
        fontSize = size
        ops += DrawOp(DrawOp.SET_FONT_SIZE, size)
    }

    override fun setStrokeWidth(size: Double) {
        ops += DrawOp(DrawOp.SET_STROKE, size)
    }

    override fun fillArc(centerX: Double, centerY: Double, radius: Double, startAngle: Double, swipeAngle: Double) {
        ops += DrawOp(DrawOp.FILL_ARC, centerX, centerY, radius, startAngle, swipeAngle)
    }

    override fun fillCircle(centerX: Double, centerY: Double, radius: Double) {
        ops += DrawOp(DrawOp.FILL_CIRCLE, centerX, centerY, radius)
    }

    override fun clearCircle(centerX: Double, centerY: Double, radius: Double) {
        ops += DrawOp(DrawOp.CLEAR_CIRCLE, centerX, centerY, radius)
    }

    override fun setTextAlign(align: TextAlign) {
        ops += DrawOp(DrawOp.SET_ALIGN, extra = align.ordinal)
    }

    override fun toImage(): Image = EmptyRecordingImage

    override fun measureText(text: String) = text.length * fontSize * 0.5

    override fun fontSpacing() = fontSize * 1.2
}

/** One recorded canvas call. Unused fields stay zero. [kind] is one of the constants below. */
class DrawOp(
    val kind: Int,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val w: Double = 0.0,
    val h: Double = 0.0,
    val a: Double = 0.0,
    val text: String = "",
    val color: Long = 0,
    val extra: Int = 0
) {
    companion object {
        const val SET_COLOR = 1
        const val LINE = 2
        const val TEXT = 3
        const val FILL_RECT = 4
        const val FILL_ROUND_RECT = 5
        const val DRAW_RECT = 6
        const val FILL_ARC = 7
        const val FILL_CIRCLE = 8
        const val CLEAR_CIRCLE = 9
        const val SET_FONT = 10
        const val SET_FONT_SIZE = 11
        const val SET_STROKE = 12
        const val SET_ALIGN = 13
    }
}

fun Color.toArgb(): Long {
    fun channel(value: Double) = (value.coerceIn(0.0, 1.0) * 255).roundToInt().toLong()
    return (channel(alpha) shl 24) or (channel(red) shl 16) or (channel(green) shl 8) or channel(blue)
}

private object EmptyRecordingImage : Image {
    override val width = 1
    override val height = 1
    override fun getPixel(x: Int, y: Int) = Color.TRANSPARENT
    override fun setPixel(x: Int, y: Int, color: Color) {}
    override suspend fun export(path: String) {}
}
