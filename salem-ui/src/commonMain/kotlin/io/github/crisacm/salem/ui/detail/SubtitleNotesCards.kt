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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.ui.screens.habits.show.views.NotesCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.SubtitleCardState
import io.github.crisacm.salem.platform.gui.FontAwesome
import io.github.crisacm.salem.ui.compose.toCompose
import io.github.crisacm.salem.ui.edit.frequencyText
import io.github.crisacm.salem.ui.edit.rememberTimeFormat
import io.github.crisacm.salem.ui.list.rememberFontAwesome
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.reminder_off
import org.jetbrains.compose.resources.stringResource

/** The subtitle of the detail screen: the question, the target, the frequency and the reminder of the habit. */
@Composable
fun SubtitleCardContent(state: SubtitleCardState, modifier: Modifier = Modifier) {
    val timeFormat = rememberTimeFormat()
    val density = LocalDensity.current
    val color = state.theme.color(state.color).toCompose()
    val muted = state.theme.mediumContrastTextColor.toCompose()
    val glyphs = rememberFontAwesome()
    // The target text has room for about seven letters, as in the old layout.
    val targetMaxWidth = with(density) { (SMALL_TEXT * 7).toDp() }
    val reminder = state.reminder
    val reminderText = if (reminder != null) {
        timeFormat.format(reminder.hour, reminder.minute)
    } else {
        stringResource(Res.string.reminder_off)
    }
    Column(modifier.fillMaxWidth().testTag("subtitleCard")) {
        if (state.question.isNotEmpty()) {
            Text(
                state.question,
                color = color,
                fontSize = REGULAR_TEXT,
                modifier = Modifier.padding(bottom = 8.dp).testTag("subtitleQuestion")
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (state.isNumerical) {
                val icon = if (state.targetType == NumericalHabitType.AT_LEAST) {
                    FontAwesome.ARROW_CIRCLE_UP
                } else {
                    FontAwesome.ARROW_CIRCLE_DOWN
                }
                Text(icon, color = muted, fontSize = 16.sp, fontFamily = glyphs)
                Text(
                    targetText(state.targetValue, state.unit),
                    color = muted,
                    fontSize = SMALL_TEXT,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp).widthIn(max = targetMaxWidth).testTag("subtitleTarget")
                )
            }
            Text(FontAwesome.CALENDAR, color = muted, fontSize = SMALL_TEXT, fontFamily = glyphs)
            Text(
                frequencyText(state.frequency.numerator, state.frequency.denominator),
                color = muted,
                fontSize = SMALL_TEXT,
                modifier = Modifier.padding(start = 4.dp, end = 16.dp).testTag("subtitleFrequency")
            )
            Text(FontAwesome.BELL_O, color = muted, fontSize = SMALL_TEXT, fontFamily = glyphs)
            Text(
                reminderText,
                color = muted,
                fontSize = SMALL_TEXT,
                modifier = Modifier.padding(start = 4.dp, top = 1.dp).testTag("subtitleReminder")
            )
        }
    }
}

/** The description of the habit, shown only when there is one. */
@Composable
fun NotesCardContent(state: NotesCardState, color: Color, modifier: Modifier = Modifier) {
    if (state.description.isEmpty()) return
    Text(
        state.description,
        color = color,
        modifier = modifier.fillMaxWidth().testTag("notesCard")
    )
}
