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

package io.github.crisacm.salem.ui.detail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.ui.screens.habits.show.views.NotesCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.SubtitleCardState
import io.github.crisacm.salem.ui.edit.frequencyText
import io.github.crisacm.salem.ui.edit.rememberTimeFormat
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.notes
import io.github.crisacm.salem.ui.resources.reminder_off
import org.jetbrains.compose.resources.stringResource

/**
 * The subtitle of the detail screen, in its header: the question as a large title, and under it pills with the
 * target, the frequency and the reminder of the habit.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubtitleCardContent(state: SubtitleCardState, modifier: Modifier = Modifier) {
    val timeFormat = rememberTimeFormat()
    val tones = rememberDetailTones(state.theme, state.color)
    val reminder = state.reminder
    val reminderText = if (reminder != null) {
        timeFormat.format(reminder.hour, reminder.minute)
    } else {
        stringResource(Res.string.reminder_off)
    }
    Column(modifier.fillMaxWidth().testTag("subtitleCard"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (state.question.isNotEmpty()) {
            Text(
                state.question,
                color = tones.onContainer,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.testTag("subtitleQuestion")
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.isNumerical) {
                val icon = if (state.targetType == NumericalHabitType.AT_LEAST) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward
                InfoPill(icon, targetText(state.targetValue, state.unit), tones.onContainer, Modifier.testTag("subtitleTarget"))
            }
            InfoPill(
                Icons.Outlined.CalendarToday,
                frequencyText(state.frequency.numerator, state.frequency.denominator),
                tones.onContainer,
                Modifier.testTag("subtitleFrequency")
            )
            InfoPill(Icons.Outlined.Notifications, reminderText, tones.onContainer, Modifier.testTag("subtitleReminder"))
        }
    }
}

/** An outlined pill with an icon and a short text. */
@Composable
private fun InfoPill(icon: ImageVector, text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .border(1.dp, color.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(text, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.padding(start = 8.dp))
    }
}

/** The description of the habit, shown only when there is one. */
@Composable
fun NotesCardContent(state: NotesCardState, color: Color, modifier: Modifier = Modifier) {
    if (state.description.isEmpty()) return
    Column(modifier.fillMaxWidth().testTag("notesCard")) {
        CardHeader(stringResource(Res.string.notes))
        Text(state.description, color = color, fontSize = 16.sp)
    }
}
