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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.list.HabitTones
import kotlin.math.min

/**
 * The longest streaks as centred pills with the number of days inside and the first and last day at each side; the
 * longest one has the full colour of the habit. [dateText] formats a day.
 */
@Composable
fun StreakBarsChart(
    bars: List<StreakBar>,
    tones: HabitTones,
    dateText: (LocalDate) -> String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val fullWidth = maxWidth.value
        val longest = min(STREAK_LONGEST_WIDTH_DP, fullWidth - 2 * STREAK_DATE_ROOM_DP)
        Column(verticalArrangement = Arrangement.spacedBy(STREAK_ROW_GAP_DP.dp), modifier = Modifier.fillMaxWidth()) {
            bars.forEach { bar ->
                val width = streakPillWidth(bar.fraction, longest)
                val showDates = streakDatesFit((fullWidth - width) / 2 - DATE_GAP_DP)
                val (container, content) = when (bar.tier) {
                    StreakTier.TOP -> tones.accent to tones.onAccent
                    StreakTier.MID -> tones.muted to tones.onContainer
                    StreakTier.LOW -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(STREAK_PILL_HEIGHT_DP.dp)) {
                    DateText(if (showDates) dateText(bar.start) else "", TextAlign.End, Modifier.weight(1f).padding(end = DATE_GAP_DP.dp), scheme.onSurfaceVariant)
                    Box(
                        Modifier.width(width.dp).height(STREAK_PILL_HEIGHT_DP.dp).clip(CircleShape).background(container),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(bar.length.toString(), color = content, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    DateText(if (showDates) dateText(bar.end) else "", TextAlign.Start, Modifier.weight(1f).padding(start = DATE_GAP_DP.dp), scheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DateText(text: String, align: TextAlign, modifier: Modifier, color: Color) {
    Text(text, color = color, fontSize = 12.sp, maxLines = 1, softWrap = false, textAlign = align, modifier = modifier)
}

private const val DATE_GAP_DP = 8f
