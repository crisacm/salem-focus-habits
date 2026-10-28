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

package io.github.crisacm.salem.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.platform.time.LocalDateFormatter

private val DayShape = RoundedCornerShape(18.dp)

/**
 * The days above the habit cards; [formatter] gives the names of the weekdays and today is the one in a tonal
 * pill. Dragging it sideways scrolls the days of the cards; the columns follow the reading order (today
 * first, or last when the sequence is reversed) and mirror in right-to-left. They line up with the cells
 * of the cards: the same margin, the same gap and the same share of the width.
 */
@Composable
fun HeaderContent(state: HeaderState, formatter: LocalDateFormatter, modifier: Modifier = Modifier) {
    val direction = scrollDirection(state.reversed, LocalLayoutDirection.current == LayoutDirection.Rtl)
    val dates = headerColumns(state.buttonCount, state.dataOffset, state.today, state.reversed)
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .exposeTestTags()
            .testTag("dayHeader")
            .draggable(
                state = rememberDraggableState { state.dragBy(it, direction) },
                orientation = Orientation.Horizontal,
                onDragStopped = { velocity -> state.fling(direction * velocity / 2) }
            )
            .padding(horizontal = 28.dp, vertical = 8.dp)
    ) {
        dates.forEach { date ->
            val isToday = date.daysUntil(state.today) == 0
            val colors = MaterialTheme.colorScheme
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(DayShape)
                    .background(if (isToday) colors.secondaryContainer else colors.background)
            ) {
                val color = when {
                    !isToday -> colors.onSurfaceVariant
                    colors.background.luminance() < 0.5f -> colors.onSecondaryContainer
                    else -> colors.primary
                }
                Text(formatter.shortWeekdayName(date).uppercase(), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(date.day.toString(), color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
