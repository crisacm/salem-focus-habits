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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardState
import io.github.crisacm.salem.core.ui.views.HistoryChart
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.canvas.CoreChart
import io.github.crisacm.salem.ui.compose.ChartScrollState
import io.github.crisacm.salem.ui.compose.Scrim
import io.github.crisacm.salem.ui.compose.toCompose

/** The calendar of the history editor is at most this tall (`history_editor_max_height` of Android). */
private val HISTORY_EDITOR_MAX_HEIGHT = 350.dp

/** The calendar takes the height that is available, up to [maxHeight]. */
fun historyEditorHeight(screenHeight: Dp, maxHeight: Dp) = if (screenHeight < maxHeight) screenHeight else maxHeight

/**
 * The calendar of a habit over a dimmed background, to edit the history: a tap on a day goes to [listener], which
 * opens the popup of that day, and a tap outside the calendar calls [onDismiss]. [state] is the one of the history
 * card, so the calendar follows the habit when the screen builds it again.
 */
@Composable
fun HistoryEditorContent(
    state: HistoryCardState,
    listener: OnDateClickedListener,
    dateFormatter: LocalDateFormatter,
    onDismiss: () -> Unit
) {
    val scroll = remember { ChartScrollState() }
    val chart = remember(state, listener, dateFormatter) {
        HistoryChart(
            dateFormatter = dateFormatter,
            firstWeekday = state.firstWeekday,
            paletteColor = state.color,
            series = state.series,
            defaultSquare = state.defaultSquare,
            notesIndicators = state.notesIndicators,
            theme = state.theme,
            today = state.today,
            onDateClickedListener = listener,
            padding = 10.0
        )
    }
    Scrim(onDismiss = onDismiss) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            CoreChart(
                chart = chart,
                scroll = scroll,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .height(historyEditorHeight(maxHeight, HISTORY_EDITOR_MAX_HEIGHT))
                    .clip(RoundedCornerShape(8.dp))
                    .background(state.theme.cardBackgroundColor.toCompose())
                    // Taps on the calendar must not reach the scrim, which dismisses the editor.
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                    .testTag("historyEditorChart")
            )
        }
    }
}
