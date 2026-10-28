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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.list.HabitTones
import kotlin.math.max

// The measures of the chart, in dp
private const val CHART_HEIGHT = 183.1f
private const val BASELINE_FROM_BOTTOM = 18f
private const val VALUE_ABOVE_BAR = 7.4f
private const val INITIAL_BASELINE_FROM_BOTTOM = 3.3f

/**
 * The history as vertical rounded bars with their number over them and the period under them. The newest bar has the
 * colour of the habit and the others its muted one. [axisLabel] names the bucket of a bar.
 */
@Composable
fun BarHistoryChart(
    bars: List<BarColumn>,
    bucketSize: Int,
    scroll: ChartScroll,
    tones: HabitTones,
    axisLabel: (bucketSize: Int, date: LocalDate) -> String,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val colors = chartColors()
    scroll.bind(bars.size, barColumnsInView(bucketSize))
    Canvas(
        modifier
            .fillMaxWidth()
            .height(CHART_HEIGHT.dp)
            .chartScrollable(scroll)
            .onSizeChanged { scroll.viewportPx = it.width.toFloat() }
    ) {
        val columnPx = scroll.columnPx
        if (columnPx <= 0f || bars.isEmpty()) return@Canvas
        val range = scroll.visible()
        val baseline = size.height - BASELINE_FROM_BOTTOM.dp.toPx()
        val scale = barScaleMax(range.map { bars[it].value })
        val barWidth = columnPx * 0.53f

        for (index in range) {
            val bar = bars[index]
            val centerX = columnLeft(index, columnPx, scroll.viewportPx, scroll.offsetPx) + columnPx / 2
            val isNewest = index == 0
            val height = max(barHeightFraction(bar.value, scale) * baseline, barWidth)
            drawRoundRect(
                if (isNewest) tones.accent else tones.muted,
                Offset(centerX - barWidth / 2, baseline - height),
                Size(barWidth, height),
                CornerRadius(barWidth / 2)
            )
            drawLabel(
                measurer,
                barValueText(bar.value),
                labelStyle(if (isNewest) tones.accent else colors.label, bold = isNewest),
                centerX,
                baseline - height - VALUE_ABOVE_BAR.dp.toPx()
            )
            drawLabel(
                measurer,
                axisLabel(bucketSize, bar.date),
                labelStyle(colors.label),
                centerX,
                size.height - INITIAL_BASELINE_FROM_BOTTOM.dp.toPx()
            )
        }
    }
}
