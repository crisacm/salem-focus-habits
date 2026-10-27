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

import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.views.HistoryChart
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordingCanvasTest {
    @Test
    fun recordsTheCallsInOrder() {
        val canvas = RecordingCanvas(100.0, 40.0)
        canvas.setColor(Color.RED)
        canvas.fillRect(1.0, 2.0, 3.0, 4.0)
        assertEquals(DrawOp.SET_COLOR, canvas.ops[0].kind)
        assertEquals(DrawOp.FILL_RECT, canvas.ops[1].kind)
        assertEquals(3.0, canvas.ops[1].w)
    }

    @Test
    fun aHistoryChartProducesPaintOperations() {
        val canvas = RecordingCanvas(320.0, 160.0)
        HistoryChart(
            dateFormatter = FixedFormatter,
            firstWeekday = DayOfWeek.SUNDAY,
            paletteColor = PaletteColor(8),
            series = listOf(HistoryChart.Square.ON, HistoryChart.Square.OFF),
            defaultSquare = HistoryChart.Square.OFF,
            notesIndicators = listOf(false, false),
            theme = LightTheme(),
            today = LocalDate(2015, 1, 25)
        ).draw(canvas)
        assertTrue(canvas.ops.any { it.kind == DrawOp.FILL_RECT || it.kind == DrawOp.FILL_ROUND_RECT })
    }
}

private object FixedFormatter : LocalDateFormatter {
    override fun narrowWeekdayName(weekday: DayOfWeek) = weekday.name.take(1)
    override fun narrowWeekdayName(date: LocalDate) = narrowWeekdayName(date.dayOfWeek)
    override fun shortWeekdayName(weekday: DayOfWeek) = weekday.name.take(2)
    override fun shortWeekdayName(date: LocalDate) = shortWeekdayName(date.dayOfWeek)
    override fun shortMonthName(date: LocalDate) = date.month.toString()
    override fun longWeekdayName(weekday: DayOfWeek) = weekday.name
    override fun longMonthName(date: LocalDate) = date.month.toString()
    override fun longFormat(date: LocalDate) = "${date.year}-${date.month}-${date.day}"
}
