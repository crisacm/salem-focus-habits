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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.list.HabitTones

/**
 * The score of a habit as a line over a filled area, with the grid at 20% steps on the left and a bubble with the
 * value of the newest point in view. [axisLabel] names a column under it; the columns come from [axisMarks].
 */
@Composable
fun ScoreLineChart(
    points: List<ScorePoint>,
    bucketSize: Int,
    scroll: ChartScroll,
    tones: HabitTones,
    axisLabel: (LocalDate) -> String,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val colors = chartColors()
    val density = LocalDensity.current
    val axisPx = with(density) { 40.dp.toPx() }
    val marks = remember(points, bucketSize) { axisMarks(points.map { it.date }, bucketSize).toSet() }
    scroll.bind(points.size, scoreColumnsInView(bucketSize))
    Canvas(
        modifier
            .fillMaxWidth()
            .height(200.dp)
            .chartScrollable(scroll)
            .onSizeChanged { scroll.viewportPx = (it.width - axisPx).coerceAtLeast(0f) }
    ) {
        val top = 10.dp.toPx()
        val bottom = size.height - 26.dp.toPx()
        val plotHeight = bottom - top
        fun yOf(value: Float) = top + (1f - value) * plotHeight

        val label = labelStyle(colors.label)
        for (tick in ScoreTicks) {
            val y = yOf(tick)
            drawLine(colors.grid, Offset(axisPx, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            val layout = measurer.measure(percentText(tick), label)
            drawText(layout, topLeft = Offset(0f, y - layout.size.height / 2f))
        }

        val columnPx = scroll.columnPx
        if (columnPx <= 0f || points.isEmpty()) return@Canvas
        val range = scroll.visible()
        val lo = (range.first - 1).coerceAtLeast(0)
        val hi = (range.last + 1).coerceAtMost(points.lastIndex)
        fun xOf(index: Int) = axisPx + columnLeft(index, columnPx, scroll.viewportPx, scroll.offsetPx) + columnPx / 2

        clipRect(left = axisPx) {
            val line = Path()
            val area = Path()
            for (index in hi downTo lo) {
                val x = xOf(index)
                val y = yOf(points[index].value)
                if (index == hi) {
                    line.moveTo(x, y)
                    area.moveTo(x, bottom)
                    area.lineTo(x, y)
                } else {
                    line.lineTo(x, y)
                    area.lineTo(x, y)
                }
            }
            area.lineTo(xOf(lo), bottom)
            area.close()
            drawPath(area, tones.accent.copy(alpha = 0.16f))
            drawPath(line, tones.accent, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            for (index in lo..hi) {
                drawCircle(tones.accent, radius = 3.5.dp.toPx(), center = Offset(xOf(index), yOf(points[index].value)))
            }

            // The newest point in view is bigger and has the value in a bubble over it
            val newest = points[range.first]
            val center = Offset(xOf(range.first), yOf(newest.value))
            drawCircle(tones.accent, radius = 6.dp.toPx(), center = center)
            val bubble = measurer.measure(percentText(newest.value), labelStyle(tones.onAccent, bold = true))
            val padding = 8.dp.toPx()
            val bubbleSize = Size(bubble.size.width + 2 * padding, bubble.size.height + 8.dp.toPx())
            val bubbleLeft = (center.x - bubbleSize.width / 2f).coerceIn(axisPx, size.width - bubbleSize.width)
            val bubbleTop = (center.y - bubbleSize.height - 10.dp.toPx()).coerceAtLeast(0f)
            drawRoundRect(tones.accent, Offset(bubbleLeft, bubbleTop), bubbleSize, CornerRadius(bubbleSize.height / 2f))
            drawText(bubble, topLeft = Offset(bubbleLeft + padding, bubbleTop + 4.dp.toPx()))
        }

        clipRect(left = axisPx) {
            for (index in range) {
                if (index in marks) drawCentered(measurer, axisLabel(points[index].date), label, xOf(index), bottom + 6.dp.toPx())
            }
        }
    }
}
