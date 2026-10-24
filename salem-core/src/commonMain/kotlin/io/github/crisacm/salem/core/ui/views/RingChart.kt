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

import io.github.crisacm.salem.platform.gui.Canvas
import io.github.crisacm.salem.platform.gui.Color
import io.github.crisacm.salem.platform.gui.Font
import io.github.crisacm.salem.platform.gui.TextAlign
import io.github.crisacm.salem.platform.gui.View
import kotlin.math.floor
import kotlin.math.min

/**
 * A ring that is filled up to [percentage] (0 to 1, in steps of [precision]), with an optional [text] at the
 * centre. The ring is as wide as [thickness] dp. The hole is painted with [holeColor]; with no colour it is
 * cut out of the canvas (see `Canvas.clearCircle`), which is what the widgets need so that their own
 * background shows through. The drawing follows the old `RingView`, with sizes in dp.
 */
class RingChart(
    var color: Color,
    var percentage: Double,
    var thickness: Double,
    var inactiveColor: Color = Color.BLACK.withAlpha(0.15),
    var holeColor: Color? = null,
    var precision: Double = 0.01,
    var text: String = "",
    var isFontAwesome: Boolean = false,
    var textSize: Double = 14.0
) : View {

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight()
        val centerX = width / 2
        val centerY = height / 2
        val radius = min(width, height) / 2
        val sweep = 360.0 * floor(percentage / precision + 0.5) * precision

        canvas.setColor(color)
        canvas.fillArc(centerX, centerY, radius, START_ANGLE, -sweep)
        canvas.setColor(inactiveColor)
        canvas.fillArc(centerX, centerY, radius, START_ANGLE - sweep, -(360.0 - sweep))
        if (thickness <= 0) return

        val hole = holeColor
        if (hole == null) {
            canvas.clearCircle(centerX, centerY, radius - thickness)
        } else {
            canvas.setColor(hole)
            canvas.fillCircle(centerX, centerY, radius - thickness)
        }
        canvas.setFont(if (isFontAwesome) Font.FONT_AWESOME else Font.REGULAR)
        canvas.setFontSize(textSize)
        canvas.setTextAlign(TextAlign.CENTER)
        canvas.setColor(color)
        canvas.drawText(text, centerX, centerY)
    }

    private companion object {
        // The ring starts at the top and goes clockwise; the canvas measures angles counterclockwise from the right.
        const val START_ANGLE = 90.0
    }
}
