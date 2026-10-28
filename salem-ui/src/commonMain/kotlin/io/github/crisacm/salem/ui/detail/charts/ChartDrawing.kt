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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** The colours that the charts share besides the tones of the habit. */
internal class ChartColors(val grid: Color, val label: Color, val dim: Color)

@Composable
internal fun chartColors(): ChartColors {
    val scheme = MaterialTheme.colorScheme
    return ChartColors(
        grid = scheme.onSurface.copy(alpha = 0.10f),
        label = scheme.onSurfaceVariant,
        dim = scheme.onSurface.copy(alpha = 0.10f)
    )
}

internal fun labelStyle(color: Color, bold: Boolean = false) = TextStyle(
    color = color,
    fontSize = 11.sp,
    fontWeight = if (bold) FontWeight.Bold else null
)

/** Draws [text] with its centre at [centerX] and its top at [top]. */
internal fun DrawScope.drawCentered(measurer: TextMeasurer, text: String, style: TextStyle, centerX: Float, top: Float) {
    val layout = measurer.measure(text, style)
    drawText(layout, topLeft = Offset(centerX - layout.size.width / 2f, top))
}
