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

package io.github.crisacm.salem.activities.common.dialogs

import android.os.Bundle
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.R
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.ui.compose.DialogButton
import io.github.crisacm.salem.ui.compose.DialogSurface

/**
 * A dialog to pick a time of the day with the Material 3 clock. The colour of the clock is the one of the
 * habit; with [newInstance]'s `showClear` the dialog also has a button that removes the time, which is what
 * the reminder of a habit needs.
 */
class TimePickerDialog : ComposeDialogFragment() {
    private var listener: OnTimePickedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The listener is not saved: a dialog restored after a rotation could not report anything.
        if (savedInstanceState != null) dismissAllowingStateLoss()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val args = requireArguments()
        val color = Color(args.getInt(KEY_COLOR))
        val state = rememberTimePickerState(
            initialHour = args.getInt(KEY_HOUR),
            initialMinute = args.getInt(KEY_MINUTE),
            is24Hour = args.getBoolean(KEY_IS_24_HOUR)
        )
        DialogSurface(
            title = null,
            onDismiss = ::dismiss,
            buttons = {
                if (args.getBoolean(KEY_SHOW_CLEAR)) {
                    DialogButton(stringResource(R.string.clear), tag = "timePickerClear") {
                        listener?.onTimeCleared()
                        dismiss()
                    }
                }
                DialogButton(stringResource(android.R.string.cancel), tag = "timePickerCancel", onClick = ::dismiss)
                DialogButton(stringResource(android.R.string.ok), tag = "timePickerOk") {
                    listener?.onTimePicked(state.hour, state.minute)
                    dismiss()
                }
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

    fun interface OnTimePickedListener {
        fun onTimePicked(hour: Int, minute: Int)

        /** The user asked to remove the time; only a dialog made with `showClear` reports it. */
        fun onTimeCleared() {}
    }

    companion object {
        private const val KEY_HOUR = "hour"
        private const val KEY_MINUTE = "minute"
        private const val KEY_IS_24_HOUR = "is24Hour"
        private const val KEY_COLOR = "color"
        private const val KEY_SHOW_CLEAR = "showClear"

        fun newInstance(
            listener: OnTimePickedListener,
            hour: Int,
            minute: Int,
            is24Hour: Boolean,
            color: Int,
            showClear: Boolean = false
        ) = TimePickerDialog().apply {
            this.listener = listener
            arguments = Bundle().apply {
                putInt(KEY_HOUR, hour)
                putInt(KEY_MINUTE, minute)
                putBoolean(KEY_IS_24_HOUR, is24Hour)
                putInt(KEY_COLOR, color)
                putBoolean(KEY_SHOW_CLEAR, showClear)
            }
        }
    }
}
