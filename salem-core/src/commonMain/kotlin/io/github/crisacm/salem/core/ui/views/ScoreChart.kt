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

import io.github.crisacm.salem.core.models.Score
import io.github.crisacm.salem.platform.gui.Canvas
import io.github.crisacm.salem.platform.gui.Color
import io.github.crisacm.salem.platform.gui.DataView
import io.github.crisacm.salem.platform.gui.Font
import io.github.crisacm.salem.platform.gui.TextAlign
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import kotlin.math.max
import kotlin.math.min

/**
 * The line chart of the score of a habit: one column per bucket (a day, a week, a month...), newest on the
 * right. [scores] has the newest first and [dataOffset] is how many columns it is scrolled into the past.
 * The drawing follows the old `ScoreChart` View, with sizes in dp.
 */
class ScoreChart(
    var theme: Theme,
    var dateFormatter: LocalDateFormatter,
    var scores: List<Score> = emptyList(),
    var color: Color = Color.BLACK,
    var bucketSize: Int = 7
) : DataView {

    override var dataOffset = 0

    // The width of a column depends on the size of the canvas, so it is known after the first draw.
    private var columnWidth = 0.0
    override val dataColumnWidth: Double
        get() = columnWidth

    private var em = 0.0
    private var fontSize = 0.0
    private var previousMonthText = ""
    private var previousYearText = ""
    private var skipYear = 0

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight().let { if (it < MIN_DRAWABLE_HEIGHT) DEFAULT_HEIGHT else it }
        canvas.setColor(theme.cardBackgroundColor)
        canvas.fill()

        fontSize = min(height * 0.06, theme.smallTextSize)
        canvas.setFont(Font.REGULAR)
        canvas.setFontSize(fontSize)
        em = canvas.fontSpacing()
        val footerHeight = 3 * em
        val paddingTop = em
        val baseSize = (height - footerHeight - paddingTop) / 8
        val widestMonth = (1..12).maxOf { canvas.measureText(dateFormatter.shortMonthName(LocalDate(2020, it, 1))) }
        var columnWidth = max(baseSize, widestMonth * 1.5)
        val nColumns = (width / columnWidth).toInt()
        if (nColumns < 1) return
        columnWidth = width / nColumns
        this.columnWidth = columnWidth
        val columnHeight = 8 * baseSize

        drawGrid(canvas, width, paddingTop, columnHeight, min(1.0, baseSize * 0.05))

        canvas.setFontSize(fontSize)
        previousMonthText = ""
        previousYearText = ""
        skipYear = 0
        var previousX = 0.0
        var previousY = 0.0
        var hasPrevious = false
        for (k in 0 until nColumns) {
            val offset = nColumns - k - 1 + dataOffset
            if (offset >= scores.size) continue
            val score = scores[offset]
            val x = k * columnWidth + columnWidth / 2
            val y = paddingTop + columnHeight - columnHeight * score.value
            if (hasPrevious) {
                canvas.setColor(color)
                canvas.setStrokeWidth(baseSize * 0.1)
                canvas.drawLine(previousX, previousY, x, y)
                drawMarker(canvas, previousX, previousY, baseSize)
            }
            if (k == nColumns - 1) drawMarker(canvas, x, y, baseSize)
            previousX = x
            previousY = y
            hasPrevious = true
            drawFooter(canvas, x, paddingTop + columnHeight, score.date)
        }
    }

    private fun text(canvas: Canvas, text: String, x: Double, baselineY: Double) {
        canvas.drawTextAtBaseline(text, x, baselineY, fontSize)
    }

    private fun drawGrid(canvas: Canvas, width: Double, top: Double, gridHeight: Double, strokeWidth: Double) {
        val nRows = 5
        val rowHeight = gridHeight / nRows
        canvas.setStrokeWidth(strokeWidth)
        canvas.setTextAlign(TextAlign.LEFT)
        var y = top
        for (i in 0 until nRows) {
            canvas.setColor(theme.mediumContrastTextColor)
            text(canvas, "${100 - i * 100 / nRows}%", 0.5 * em, y + em)
            canvas.setColor(theme.lowContrastTextColor)
            canvas.drawLine(0.0, y, width, y)
            y += rowHeight
        }
        canvas.drawLine(0.0, y, width, y)
    }

    // A marker is a disc of the colour of the card with a smaller disc of the colour of the chart on top of it.
    // When the card is transparent (the widgets) the first disc is a hole, so the line does not show through.
    private fun drawMarker(canvas: Canvas, x: Double, y: Double, baseSize: Double) {
        if (theme.cardBackgroundColor.alpha == 0.0) {
            canvas.clearCircle(x, y, baseSize * 0.275)
        } else {
            canvas.setColor(theme.cardBackgroundColor)
            canvas.fillCircle(x, y, baseSize * 0.275)
        }
        canvas.setColor(color)
        canvas.fillCircle(x, y, baseSize * 0.175)
    }

    private fun drawFooter(canvas: Canvas, centerX: Double, bottom: Double, date: LocalDate) {
        val yearText = date.year.toString()
        val monthText = dateFormatter.shortMonthName(date)
        var shouldPrintYear = true
        if (yearText == previousYearText) shouldPrintYear = false
        if (bucketSize >= 365 && date.year % 2 != 0) shouldPrintYear = false
        if (skipYear > 0) {
            skipYear--
            shouldPrintYear = false
        }
        canvas.setColor(theme.mediumContrastTextColor)
        canvas.setTextAlign(TextAlign.CENTER)
        if (shouldPrintYear) {
            previousYearText = yearText
            previousMonthText = ""
            text(canvas, yearText, centerX, bottom + em * 2.2)
            skipYear = 1
        }
        if (bucketSize < 365) {
            val text = if (monthText != previousMonthText) {
                previousMonthText = monthText
                monthText
            } else {
                date.day.toString()
            }
            text(canvas, text, centerX, bottom + em * 1.2)
        }
    }

    private companion object {
        const val MIN_DRAWABLE_HEIGHT = 9.0
        const val DEFAULT_HEIGHT = 200.0
    }
}
