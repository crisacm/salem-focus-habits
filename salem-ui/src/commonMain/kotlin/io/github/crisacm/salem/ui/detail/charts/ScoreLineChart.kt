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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.list.HabitTones
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// The measures of the chart, in dp (the chart is as wide as the card without its padding)
private const val CHART_HEIGHT = 226.6f
private const val PLOT_BOTTOM = 196.8f
private const val PLOT_HEIGHT = 169.6f
private const val VIEWPORT_LEFT = 38.2f
private const val VIEWPORT_RIGHT_INSET = 3f
private const val LABEL_ABOVE_LINE = 6.3f
private const val AXIS_BASELINE = 216.3f
private const val BUBBLE_HEIGHT = 27.6f
private const val BUBBLE_PADDING = 12f

/**
 * The score of a habit as a line over a filled area, with the grid at 20% steps and the name of each step over its
 * line, and a bubble with the value of the newest point in view. Under it, for buckets of a month or less, there are
 * texts 40 dp apart (the month where it changes, the day in between); for longer buckets each column has its own.
 */
@Composable
fun ScoreLineChart(
    points: List<ScorePoint>,
    bucketSize: Int,
    scroll: ChartScroll,
    tones: HabitTones,
    monthName: (LocalDate) -> String,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val colors = chartColors()
    val density = LocalDensity.current
    val leftPx = with(density) { VIEWPORT_LEFT.dp.toPx() }
    val rightPx = with(density) { VIEWPORT_RIGHT_INSET.dp.toPx() }
    scroll.bind(points.size, scoreColumnsInView(bucketSize))
    Canvas(
        modifier
            .fillMaxWidth()
            .height(CHART_HEIGHT.dp)
            .chartScrollable(scroll)
            .onSizeChanged { scroll.viewportPx = (it.width - leftPx - rightPx).coerceAtLeast(0f) }
    ) {
        val plotBottom = PLOT_BOTTOM.dp.toPx()
        val plotHeight = PLOT_HEIGHT.dp.toPx()
        fun yOf(value: Float) = plotBottom - value * plotHeight

        val label = labelStyle(colors.label)
        for (tick in ScoreTicks) {
            val y = yOf(tick)
            drawLine(colors.grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            drawLabel(measurer, percentText(tick), label, 0f, y - LABEL_ABOVE_LINE.dp.toPx(), LabelAlign.START)
        }

        val columnPx = scroll.columnPx
        if (columnPx <= 0f || points.isEmpty()) return@Canvas
        val range = scroll.visible()
        val lo = max(range.first - 1, 0)
        val hi = min(range.last + 1, points.lastIndex)
        fun xOf(index: Int) = leftPx + columnLeft(index, columnPx, scroll.viewportPx, scroll.offsetPx) + columnPx / 2
        val viewportRight = leftPx + scroll.viewportPx

        clipRect(left = leftPx) {
            val line = Path()
            val area = Path()
            for (index in hi downTo lo) {
                val x = xOf(index)
                val y = yOf(points[index].value)
                if (index == hi) {
                    line.moveTo(x, y)
                    area.moveTo(x, plotBottom)
                    area.lineTo(x, y)
                } else {
                    line.lineTo(x, y)
                    area.lineTo(x, y)
                }
            }
            area.lineTo(xOf(lo), plotBottom)
            area.close()
            drawPath(area, tones.accent.copy(alpha = 0.14f))
            drawPath(line, tones.accent, style = Stroke(3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            for (index in lo..hi) {
                drawCircle(tones.accent, radius = 3.2.dp.toPx(), center = Offset(xOf(index), yOf(points[index].value)))
            }

            // The newest point in view has a larger dot, and the value in a bubble that sits on it
            val newest = points[range.first]
            val center = Offset(xOf(range.first), yOf(newest.value))
            val dot = 5.3.dp.toPx()
            drawCircle(tones.accent, radius = dot, center = center)
            val text = measurer.measure(percentText(newest.value), labelStyle(tones.onAccent, bold = true))
            val bubbleWidth = text.size.width + 2 * BUBBLE_PADDING.dp.toPx()
            val bubbleHeight = BUBBLE_HEIGHT.dp.toPx()
            val bubbleLeft = min(center.x - bubbleWidth / 2f, viewportRight - bubbleWidth).coerceAtLeast(leftPx)
            val bubbleTop = (center.y - dot - bubbleHeight).coerceAtLeast(0f)
            drawRoundRect(tones.accent, Offset(bubbleLeft, bubbleTop), Size(bubbleWidth, bubbleHeight), CornerRadius(bubbleHeight / 2f))
            drawLabel(
                measurer,
                percentText(newest.value),
                labelStyle(tones.onAccent, bold = true),
                bubbleLeft + bubbleWidth / 2f,
                bubbleTop + bubbleHeight / 2f + 4.3.dp.toPx()
            )
        }

        val axisBaseline = AXIS_BASELINE.dp.toPx()
        if (bucketSize >= 92) {
            for (index in range) {
                drawLabel(measurer, scoreAxisLabel(bucketSize, points[index].date, monthName), label, xOf(index), axisBaseline)
            }
        } else {
            val spacing = SCORE_AXIS_SPACING_DP.dp.toPx()
            val xs = generateSequence(leftPx + columnPx / 2 - 2.3.dp.toPx()) { it + spacing }
                .takeWhile { it + 12.dp.toPx() < viewportRight }
                .toList()
            // Each text names the date of the column that is nearest to its place
            val dates = xs.map { x -> points[range.minByOrNull { abs(xOf(it) - x) } ?: range.first].date }
            // The oldest is at the left, and the columns of the points are newest first
            scoreAxisTexts(dates, monthName).forEachIndexed { index, text -> drawLabel(measurer, text, label, xs[index], axisBaseline) }
        }
    }
}
