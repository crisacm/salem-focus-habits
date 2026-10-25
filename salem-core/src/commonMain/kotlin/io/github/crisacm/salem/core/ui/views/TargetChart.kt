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
import kotlin.math.max
import kotlin.math.min

/**
 * The progress of a habit towards its targets as horizontal bars, one row per interval (today, week,
 * month...). Each row is [ROW_HEIGHT] dp tall, so the chart needs [height] dp. [formatValue] writes the
 * numbers inside the bars. The drawing follows the old `TargetChart` View, with sizes in dp.
 */
class TargetChart(
    var theme: Theme,
    var labels: List<String> = emptyList(),
    var values: List<Double> = emptyList(),
    var targets: List<Double> = emptyList(),
    var color: Color = Color.BLACK,
    var formatValue: (Double) -> String = { it.toString() },
    // With stretchRows the rows share the whole height (a widget); otherwise each one is ROW_HEIGHT dp tall.
    var stretchRows: Boolean = false
) : View {

    val height: Double
        get() = labels.size * ROW_HEIGHT

    override fun draw(canvas: Canvas) {
        canvas.setColor(theme.cardBackgroundColor)
        canvas.fill()
        if (labels.isEmpty()) return

        val fontSize = theme.smallTextSize
        canvas.setFont(Font.REGULAR)
        canvas.setFontSize(fontSize)
        val maxLabelWidth = labels.maxOf { canvas.measureText(it) }
        val width = canvas.getWidth()
        val rowHeight = if (stretchRows) canvas.getHeight() / labels.size else ROW_HEIGHT
        var top = (canvas.getHeight() - rowHeight * labels.size) / 2
        for (row in labels.indices) {
            drawRow(canvas, width, top, rowHeight, row, maxLabelWidth)
            top += rowHeight
        }
    }

    private fun drawRow(canvas: Canvas, width: Double, top: Double, rowHeight: Double, row: Int, maxLabelWidth: Double) {
        val stop = maxLabelWidth + 2 * PADDING
        val centerY = top + rowHeight / 2
        // The old chart centred the text with the ascent and descent of the font; the canvas centres it by itself.
        canvas.setColor(theme.mediumContrastTextColor)
        canvas.setTextAlign(TextAlign.RIGHT)
        canvas.drawText(labels[row], stop - PADDING, centerY)

        val barLeft = stop + PADDING
        val barTop = top + 0.05 * rowHeight
        val barWidth = width - PADDING - barLeft
        val barHeight = rowHeight * 0.9
        canvas.setColor(theme.lowContrastTextColor)
        canvas.fillRoundRect(barLeft, barTop, barWidth, barHeight, ROUND)

        val target = targets[row]
        val value = values[row]
        val percentage = min(1.0, if (target > 0) value / target else 1.0)
        var completedWidth = percentage * barWidth
        if (completedWidth > 0 && completedWidth < 2 * ROUND) completedWidth = 2 * ROUND
        val remainingWidth = barWidth - completedWidth
        canvas.setColor(color)
        canvas.fillRoundRect(barLeft, barTop, completedWidth, barHeight, ROUND)

        canvas.setTextAlign(TextAlign.CENTER)
        canvas.setFontSize(theme.smallTextSize)
        val completedText = formatValue(value)
        val remainingText = formatValue(max(0.0, target - value))
        if (completedWidth > canvas.measureText(completedText) + 2 * PADDING) {
            canvas.setColor(theme.highContrastReverseTextColor)
            canvas.drawText(completedText, barLeft + completedWidth / 2, centerY)
        }
        if (remainingWidth > canvas.measureText(remainingText) + 2 * PADDING) {
            canvas.setColor(theme.mediumContrastTextColor)
            canvas.drawText(remainingText, barLeft + completedWidth + remainingWidth / 2, centerY)
        }
    }

    companion object {
        const val ROW_HEIGHT = 20.0
        private const val PADDING = 4.0
        private const val ROUND = 2.0
    }
}
