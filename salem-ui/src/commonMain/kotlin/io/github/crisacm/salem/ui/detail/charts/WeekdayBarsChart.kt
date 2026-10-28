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
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.ui.list.HabitTones

/**
 * The completions of each day of the week as horizontal rounded bars with the count at the end; the day with the
 * most completions has the full colour of the habit. [dayName] gives the short name of a day.
 */
@Composable
fun WeekdayBarsChart(
    bars: List<WeekdayBar>,
    tones: HabitTones,
    dayName: (DayOfWeek) -> String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val max = bars.maxOfOrNull { it.total } ?: 0
    val highlighted = highlightedWeekday(bars)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        bars.forEachIndexed { index, bar ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(dayName(bar.day), color = scheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.width(45.6.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(scheme.surfaceContainerHighest)
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(maxOf(barFraction(bar.total, max), MIN_FILL))
                            .clip(CircleShape)
                            .background(if (index == highlighted) tones.accent else tones.muted)
                    )
                }
                Text(
                    bar.total.toString(),
                    color = if (index == highlighted) tones.accent else scheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp).padding(start = 8.dp)
                )
            }
        }
    }
}

/** Even a day with no completions keeps a stub of colour, so its bar is not mistaken for an empty track. */
private const val MIN_FILL = 0.08f
