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

package io.github.crisacm.salem.activities.habits.list

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.R
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import java.util.Locale

/**
 * The row of days above the habit list. Dragging it sideways scrolls the days of the rows; the columns
 * follow the reading order (today first, or last when the sequence is reversed) and mirror in right-to-left.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HeaderContent(state: HeaderState, background: Color, textColor: Color) {
    val direction = scrollDirection(state.reversed, LocalLayoutDirection.current == LayoutDirection.Rtl)
    val formatter = remember { JavaLocalDateFormatter(Locale.getDefault()) }
    val columnWidth = dimensionResource(R.dimen.checkmarkWidth)
    val dates = headerColumns(state.buttonCount, state.dataOffset, state.today, state.reversed)
    Surface(
        color = background,
        shadowElevation = 2.dp,
        modifier = Modifier.semantics { testTagsAsResourceId = true }
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.checkmarkHeight))
                .background(background)
                .testTag("dayHeader")
                .draggable(
                    state = rememberDraggableState { state.dragBy(it, direction) },
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity -> state.fling(direction * velocity / 2) }
                )
                .padding(end = 3.dp)
        ) {
            dates.forEach { date ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.width(columnWidth).fillMaxHeight()
                ) {
                    Text(formatter.shortWeekdayName(date).uppercase(), color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(date.day.toString(), color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
