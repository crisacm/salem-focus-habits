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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.ui.list.HabitTones

/** One row of the targets: its [label], the [value] reached and the [target] to reach; a row with nothing reached has no fill. */
class TargetRow(val label: String, val value: Double, val target: Double)

/**
 * The progress towards each target as a pill track filled with the colour of the habit; the number in the middle of
 * the track is what is left to reach the target.
 */
@Composable
fun TargetBarsChart(
    rows: List<TargetRow>,
    tones: HabitTones,
    formatValue: (Double) -> String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(TARGET_ROW_GAP_DP.dp), modifier = modifier.fillMaxWidth()) {
        rows.forEach { row ->
            val fraction = targetFraction(row.value, row.target)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(row.label, color = scheme.onSurfaceVariant, fontSize = 14.sp, maxLines = 1, modifier = Modifier.width(TARGET_LABEL_WIDTH_DP.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .height(TARGET_TRACK_HEIGHT_DP.dp)
                        .clip(CircleShape)
                        .background(scheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    if (fraction > 0f) {
                        Box(
                            Modifier
                                .align(Alignment.CenterStart)
                                .padding(TARGET_FILL_INSET_DP.dp)
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .widthIn(min = TARGET_FILL_MIN_WIDTH_DP.dp)
                                .clip(CircleShape)
                                .background(tones.accent)
                        )
                    }
                    Text(
                        formatValue(targetRemaining(row.value, row.target)),
                        color = if (targetTextOnFill(fraction)) tones.onAccent else scheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
