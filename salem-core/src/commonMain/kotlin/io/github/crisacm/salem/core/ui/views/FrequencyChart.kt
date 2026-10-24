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
import io.github.crisacm.salem.platform.gui.DataView
import io.github.crisacm.salem.platform.gui.Font
import io.github.crisacm.salem.platform.gui.TextAlign
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.platform.time.countWeekdayOccurrencesInMonth
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.platform.time.getWeekdaySequence
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The frequency of a habit by weekday, one column per month, newest on the right: a circle per weekday
 * whose size and tone grow with how often it was done. [frequency] maps the first day of a month to the
 * count of each weekday (index 0 is Sunday) and [dataOffset] is how many months it is scrolled into the
 * past. The drawing follows the old `FrequencyChart` View, with sizes in dp.
 */
class FrequencyChart(
    var theme: Theme,
    var dateFormatter: LocalDateFormatter,
    var frequency: Map<LocalDate, Array<Int>> = emptyMap(),
    var color: Color = Color.BLACK,
    var isNumerical: Boolean = false,
    var firstWeekday: DayOfWeek = DayOfWeek.SUNDAY
) : DataView {

    override var dataOffset = 0

    // The width of a column depends on the size of the canvas, so it is known after the first draw.
    private var columnWidth = 0.0
    override val dataColumnWidth: Double
        get() = columnWidth

    private var em = 0.0
    private var fontSize = 0.0

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight().let { if (it < MIN_DRAWABLE_HEIGHT) DEFAULT_HEIGHT else it }
        canvas.setColor(theme.cardBackgroundColor)
        canvas.fill()

        val baseSize = height / 8
        fontSize = baseSize * 0.4
        canvas.setFont(Font.REGULAR)
        canvas.setFontSize(fontSize)
        em = canvas.fontSpacing()
        val widestMonth = (1..12).maxOf { canvas.measureText(dateFormatter.shortMonthName(LocalDate(2020, it, 1))) }
        columnWidth = max(baseSize, widestMonth * 1.2)
        val columnHeight = 8 * baseSize
        val nColumns = (width / columnWidth).toInt()
        if (nColumns < 1) return

        val colors = markerColors()
        val maxFrequency = frequency.values.maxOfOrNull { it.max() }?.coerceAtLeast(1) ?: 1

        drawGrid(canvas, width, columnHeight / 8)

        // The first column of the old chart was one month further into the past than the last one drawn.
        var date = firstOfMonth(getToday(), -nColumns + 2 - dataOffset)
        for (i in 0 until nColumns - 1) {
            drawColumn(canvas, i * columnWidth, baseSize, date, colors, maxFrequency)
            date = firstOfMonth(date, 1)
        }
    }

    private fun drawColumn(
        canvas: Canvas,
        left: Double,
        baseSize: Double,
        date: LocalDate,
        colors: List<Color>,
        maxFrequency: Int
    ) {
        val values = frequency[date]
        val weekdaysInMonth = countWeekdayOccurrencesInMonth(date)
        if (values != null) {
            getWeekdaySequence(firstWeekday).forEachIndexed { j, weekday ->
                val i = (weekday.daysSinceSunday + 1) % 7
                val centerX = left + baseSize / 2
                val centerY = baseSize * j + baseSize / 2
                drawMarker(canvas, centerX, centerY, baseSize, values[i], weekdaysInMonth[i], colors, maxFrequency)
            }
        }
        drawFooter(canvas, left + baseSize / 2, baseSize * 7.5, date)
    }

    private fun drawMarker(
        canvas: Canvas,
        centerX: Double,
        centerY: Double,
        baseSize: Double,
        value: Int,
        weekdayFrequency: Int,
        colors: List<Color>,
        maxFrequency: Int
    ) {
        // The value can be negative when the entry is skipped.
        val padding = baseSize * 0.2
        val maxRadius = (baseSize - 2 * padding) / 2
        val scalingFactor = if (isNumerical) maxFrequency else weekdayFrequency
        val scale = max(0, value).toDouble() / scalingFactor
        val colorIndex = min(colors.size - 1, ((colors.size - 1) * scale).roundToInt())
        canvas.setColor(colors[colorIndex])
        canvas.fillCircle(centerX, centerY, maxRadius * scale)
    }

    private fun drawFooter(canvas: Canvas, centerX: Double, centerY: Double, date: LocalDate) {
        canvas.setColor(theme.mediumContrastTextColor)
        canvas.setTextAlign(TextAlign.CENTER)
        canvas.setFontSize(fontSize)
        text(canvas, dateFormatter.shortMonthName(date), centerX, centerY - 0.1 * em)
        if (date.month == 2) text(canvas, date.year.toString(), centerX, centerY + 0.9 * em)
    }

    private fun drawGrid(canvas: Canvas, width: Double, rowHeight: Double) {
        canvas.setTextAlign(TextAlign.LEFT)
        canvas.setStrokeWidth(1.0)
        var y = 0.0
        for (day in getWeekdaySequence(firstWeekday)) {
            canvas.setColor(theme.mediumContrastTextColor)
            text(canvas, dateFormatter.shortWeekdayName(day), width - columnWidth, y + rowHeight / 2 + 0.25 * em)
            canvas.setColor(theme.lowContrastTextColor)
            canvas.drawLine(0.0, y, width, y)
            y += rowHeight
        }
        canvas.drawLine(0.0, y, width, y)
    }

    // From the grid colour (nothing) to the colour of the chart (always), in four steps.
    private fun markerColors(): List<Color> {
        val grid = theme.lowContrastTextColor
        return listOf(grid, grid.blendWith(color, 0.34), grid.blendWith(color, 0.67), color)
    }

    // The old chart gave the baseline of the text; the canvas takes about the middle of the lower case letters.
    private fun text(canvas: Canvas, text: String, x: Double, baselineY: Double) {
        canvas.drawText(text, x, baselineY - BASELINE_TO_MIDDLE * fontSize)
    }

    private fun firstOfMonth(date: LocalDate, months: Int): LocalDate {
        var year = date.year
        var month = date.month + months
        while (month < 1) {
            month += 12
            year -= 1
        }
        while (month > 12) {
            month -= 12
            year += 1
        }
        return LocalDate(year, month, 1)
    }

    private companion object {
        const val MIN_DRAWABLE_HEIGHT = 9.0
        const val DEFAULT_HEIGHT = 200.0
        const val BASELINE_TO_MIDDLE = 0.3
    }
}
