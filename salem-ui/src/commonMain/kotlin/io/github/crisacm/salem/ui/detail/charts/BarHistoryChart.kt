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

/**
 * The history as vertical rounded bars with their number on top and the period under them. The newest bar has
 * the full colour of the habit and the others a softer one. [axisLabel] names the bucket of a bar.
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
            .height(190.dp)
            .chartScrollable(scroll)
            .onSizeChanged { scroll.viewportPx = it.width.toFloat() }
    ) {
        val columnPx = scroll.columnPx
        if (columnPx <= 0f || bars.isEmpty()) return@Canvas
        val range = scroll.visible()
        val valueRoom = 20.dp.toPx()
        val axisRoom = 24.dp.toPx()
        val baseline = size.height - axisRoom
        val barArea = baseline - valueRoom
        val scale = barScaleMax(range.map { bars[it].value })
        val barWidth = columnPx * 0.56f

        for (index in range) {
            val bar = bars[index]
            val centerX = columnLeft(index, columnPx, scroll.viewportPx, scroll.offsetPx) + columnPx / 2
            val isNewest = index == 0
            val height = maxOf((bar.value / scale).toFloat() * barArea, barWidth)
            val color = if (isNewest) tones.accent else tones.accent.copy(alpha = 0.45f)
            drawRoundRect(
                color,
                Offset(centerX - barWidth / 2, baseline - height),
                Size(barWidth, height),
                CornerRadius(barWidth / 2)
            )
            drawCentered(
                measurer,
                barValueText(bar.value),
                labelStyle(if (isNewest) tones.accent else colors.label, bold = isNewest),
                centerX,
                baseline - height - 16.dp.toPx()
            )
            drawCentered(measurer, axisLabel(bucketSize, bar.date), labelStyle(colors.label), centerX, baseline + 6.dp.toPx())
        }
    }
}
