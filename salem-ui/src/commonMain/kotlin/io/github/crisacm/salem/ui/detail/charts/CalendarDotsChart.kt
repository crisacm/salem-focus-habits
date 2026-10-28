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

package io.github.crisacm.salem.ui.detail.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.list.HabitTones

// The measures of the chart, in dp
private val LettersWidth = 16.dp
private val MonthsHeight = 32.dp
private val RowHeight = 17.dp
private const val DOT_RATIO = 0.88f
private const val MONTH_BASELINE = 23.5f
private const val LETTER_START = 2.8f

/**
 * The days of the history as round dots, a column per week, with the letters of the weekdays at the left and the
 * names of the months over the weeks. [letters] are in the order of [columns]; [monthName] names a month.
 */
@Composable
fun CalendarDotsChart(
    columns: List<CalendarColumn>,
    letters: List<String>,
    scroll: ChartScroll,
    tones: HabitTones,
    monthName: (LocalDate) -> String,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val colors = chartColors()
    val labels = remember(columns) { monthLabels(columns).associateBy { it.column } }
    scroll.bind(columns.size, CALENDAR_COLUMNS_IN_VIEW)
    Row(modifier.fillMaxWidth()) {
        Canvas(Modifier.width(LettersWidth).height(7 * RowHeight + MonthsHeight)) {
            val rowHeight = RowHeight.toPx()
            val style = labelStyle(colors.label)
            letters.forEachIndexed { row, letter ->
                // The baseline of a letter is a third of its height under the centre of its row
                val center = MonthsHeight.toPx() + row * rowHeight + rowHeight / 2
                drawLabel(measurer, letter, style, LETTER_START.dp.toPx(), center + 4.3.dp.toPx(), LabelAlign.START)
            }
        }
        Canvas(
            Modifier
                .weight(1f)
                .height(7 * RowHeight + MonthsHeight)
                .chartScrollable(scroll)
                .onSizeChanged { scroll.viewportPx = it.width.toFloat() }
        ) {
            val columnPx = scroll.columnPx
            if (columnPx <= 0f || columns.isEmpty()) return@Canvas
            val rowHeight = RowHeight.toPx()
            val diameter = minOf(columnPx, rowHeight) * DOT_RATIO
            val top = MonthsHeight.toPx()
            clipRect {
                for (index in scroll.visible()) {
                    val left = columnLeft(index, columnPx, scroll.viewportPx, scroll.offsetPx)
                    val centerX = left + columnPx / 2
                    labels[index]?.let {
                        drawLabel(measurer, monthName(it.date), labelStyle(colors.label), centerX, MONTH_BASELINE.dp.toPx())
                    }
                    columns[index].dots.forEachIndexed { row, dot ->
                        if (dot == null) return@forEachIndexed
                        val center = Offset(centerX, top + row * rowHeight + rowHeight / 2)
                        when (dot.state) {
                            DotState.DONE -> drawCircle(tones.accent, diameter / 2, center)
                            DotState.SOFT -> drawCircle(tones.muted, diameter / 2, center)
                            DotState.SKIPPED -> drawCircle(
                                tones.accent.copy(alpha = 0.6f),
                                diameter / 2 - 1.dp.toPx(),
                                center,
                                style = Stroke(1.5.dp.toPx())
                            )
                            DotState.MISSED -> drawCircle(colors.dim, diameter / 2, center)
                        }
                    }
                }
            }
        }
    }
}
