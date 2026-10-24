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

package io.github.crisacm.salem.activities.habits.show.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.habits.edit.formatFrequency
import io.github.crisacm.salem.activities.habits.show.targetText
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.ui.screens.habits.show.views.NotesCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.SubtitleCardState
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.utils.InterfaceUtils.getFontAwesome
import io.github.crisacm.salem.utils.formatTime

/** The subtitle of the detail screen: the question, the target, the frequency and the reminder of the habit. */
@Composable
fun SubtitleCardContent(state: SubtitleCardState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val color = Color(state.theme.color(state.color).toInt())
    val muted = Color(state.theme.mediumContrastTextColor.toInt())
    val glyphs = remember { getFontAwesome(context)?.let { FontFamily(it) } }
    val small = with(density) { dimensionResource(R.dimen.smallTextSize).toSp() }
    val regular = with(density) { dimensionResource(R.dimen.regularTextSize).toSp() }
    // The target text has room for about seven letters, as in the old layout.
    val targetMaxWidth = with(density) { (small * 7).toDp() }
    val reminder = state.reminder
    val reminderText = if (reminder != null) {
        formatTime(context, reminder.hour, reminder.minute).orEmpty()
    } else {
        stringResource(R.string.reminder_off)
    }
    Column(modifier.fillMaxWidth().testTag("subtitleCard")) {
        if (state.question.isNotEmpty()) {
            Text(
                state.question,
                color = color,
                fontSize = regular,
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
                    R.string.fa_arrow_circle_up
                } else {
                    R.string.fa_arrow_circle_down
                }
                Text(stringResource(icon), color = muted, fontSize = 16.sp, fontFamily = glyphs)
                Text(
                    targetText(state.targetValue, state.unit),
                    color = muted,
                    fontSize = small,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp).widthIn(max = targetMaxWidth).testTag("subtitleTarget")
                )
            }
            Text(stringResource(R.string.fa_calendar), color = muted, fontSize = small, fontFamily = glyphs)
            Text(
                formatFrequency(state.frequency.numerator, state.frequency.denominator, context.resources),
                color = muted,
                fontSize = small,
                modifier = Modifier.padding(start = 4.dp, end = 16.dp).testTag("subtitleFrequency")
            )
            Text(stringResource(R.string.fa_bell_o), color = muted, fontSize = small, fontFamily = glyphs)
            Text(
                reminderText,
                color = muted,
                fontSize = small,
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
