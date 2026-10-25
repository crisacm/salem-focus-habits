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

package io.github.crisacm.salem.ui.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.ui.compose.DialogButton
import io.github.crisacm.salem.ui.compose.DialogSurface
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.cancel
import io.github.crisacm.salem.ui.resources.clear
import io.github.crisacm.salem.ui.resources.ok
import org.jetbrains.compose.resources.stringResource

/**
 * The Material 3 clock, in the [color] of the habit. [onPicked] gets the hour and minute; with [showClear] a button
 * calls [onCleared], which is what the reminder of a habit needs to remove its time. The host closes the dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerContent(
    hour: Int,
    minute: Int,
    is24Hour: Boolean,
    color: Color,
    showClear: Boolean,
    onPicked: (hour: Int, minute: Int) -> Unit,
    onCleared: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = stringResource(Res.string.ok),
    cancelLabel: String = stringResource(Res.string.cancel)
) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = is24Hour)
    DialogSurface(
        title = null,
        onDismiss = onDismiss,
        buttons = {
            if (showClear) DialogButton(stringResource(Res.string.clear), tag = "timePickerClear", onClick = onCleared)
            DialogButton(cancelLabel, tag = "timePickerCancel", onClick = onDismiss)
            DialogButton(confirmLabel, tag = "timePickerOk") { onPicked(state.hour, state.minute) }
        }
    ) {
        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    selectorColor = color,
                    timeSelectorSelectedContainerColor = color.copy(alpha = 0.2f)
                ),
                modifier = Modifier.testTag("timePicker")
            )
        }
    }
}
