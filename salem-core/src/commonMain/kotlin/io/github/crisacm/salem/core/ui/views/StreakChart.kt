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

import io.github.crisacm.salem.core.models.Streak
import io.github.crisacm.salem.platform.gui.Canvas
import io.github.crisacm.salem.platform.gui.Color
import io.github.crisacm.salem.platform.gui.Font
import io.github.crisacm.salem.platform.gui.TextAlign
import io.github.crisacm.salem.platform.gui.View
import io.github.crisacm.salem.platform.time.LocalDate
import kotlin.math.max

/**
 * The best streaks of a habit as horizontal bars, longest first. Each row is [ROW_HEIGHT] dp tall, so the
 * chart needs [height] dp. [formatDate] writes the first and last day of a streak, which are shown at the
 * sides of its bar when there is room. The drawing follows the old `StreakChart` View, with sizes in dp.
 */
class StreakChart(
    var theme: Theme,
    var formatDate: (LocalDate) -> String,
    var streaks: List<Streak> = emptyList(),
    var color: Color = Color.BLACK
) : View {

    val height: Double
        get() = streaks.size * ROW_HEIGHT

    override fun draw(canvas: Canvas) {
        canvas.setColor(theme.cardBackgroundColor)
        canvas.fill()
        if (streaks.isEmpty()) return

        val width = canvas.getWidth()
        val fontSize = (ROW_HEIGHT * 0.5).coerceIn(theme.smallTextSize, theme.regularTextSize)
        canvas.setFont(Font.REGULAR)
        canvas.setFontSize(fontSize)
        val em = canvas.fontSpacing()
        val textMargin = 0.5 * em
        val maxLength = streaks.maxOf { it.length }
        if (maxLength == 0) return

        // The dates are left out when they would take more than three quarters of the width.
        var maxLabelWidth = streaks.maxOf {
            max(canvas.measureText(formatDate(it.start)), canvas.measureText(formatDate(it.end)))
        }
        if (width - 2 * maxLabelWidth < 0.25 * width) maxLabelWidth = 0.0

        var top = 0.0
        for (streak in streaks) {
            drawRow(canvas, width, top, streak, maxLength, maxLabelWidth, em, textMargin, fontSize)
            top += ROW_HEIGHT
        }
    }

    private fun drawRow(
        canvas: Canvas,
        width: Double,
        top: Double,
        streak: Streak,
        maxLength: Int,
        maxLabelWidth: Double,
        em: Double,
        textMargin: Double,
        fontSize: Double
    ) {
        val percentage = streak.length.toDouble() / maxLength
        var availableWidth = width - 2 * maxLabelWidth
        if (maxLabelWidth > 0) availableWidth -= 2 * textMargin
        val lengthText = streak.length.toString()
        val barWidth = max(percentage * availableWidth, canvas.measureText(lengthText) + em)
        val gap = (width - barWidth) / 2
        val padding = ROW_HEIGHT * 0.05

        canvas.setColor(barColor(percentage))
        canvas.fillRoundRect(gap, top + padding, barWidth, ROW_HEIGHT - 2 * padding, CORNER_RADIUS)

        val baseline = top + ROW_HEIGHT / 2 + 0.3 * em
        canvas.setColor(if (percentage >= 0.5) theme.highContrastReverseTextColor else theme.mediumContrastTextColor)
        canvas.setTextAlign(TextAlign.CENTER)
        canvas.drawTextAtBaseline(lengthText, width / 2, baseline, fontSize)

        if (maxLabelWidth > 0) {
            canvas.setColor(theme.mediumContrastTextColor)
            canvas.setTextAlign(TextAlign.RIGHT)
            canvas.drawTextAtBaseline(formatDate(streak.start), gap - textMargin, baseline, fontSize)
            canvas.setTextAlign(TextAlign.LEFT)
            canvas.drawTextAtBaseline(formatDate(streak.end), width - gap + textMargin, baseline, fontSize)
        }
    }

    private fun barColor(percentage: Double) = when {
        percentage >= 1.0 -> color
        percentage >= 0.8 -> color.withAlpha(192 / 255.0)
        percentage >= 0.5 -> color.withAlpha(96 / 255.0)
        else -> theme.lowContrastTextColor
    }

    companion object {
        const val ROW_HEIGHT = 20.0
        private const val CORNER_RADIUS = 2.0
    }
}
