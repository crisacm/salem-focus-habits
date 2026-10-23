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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.ui.compose.DialogButton
import io.github.crisacm.salem.ui.compose.DialogSurface
import java.util.Locale

/**
 * Dialog that allows the user to pick one or more days of the week.
 */
class WeekdayPickerDialog : ComposeDialogFragment() {
    private var selectedDays = BooleanArray(7)
    private var listener: OnWeekdaysPickedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getBooleanArray(KEY_SELECTED_DAYS)?.let { selectedDays = it }
        // The listener is not saved: a dialog restored after a rotation could not report anything.
        if (savedInstanceState != null) dismissAllowingStateLoss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray(KEY_SELECTED_DAYS, selectedDays)
    }

    @Composable
    override fun Content() {
        // Names start at Saturday, as the weekday list does.
        val names = remember { JavaLocalDateFormatter(Locale.getDefault()).longWeekdayNames(DayOfWeek.SATURDAY) }
        val checked = remember { mutableStateListOf(*selectedDays.toTypedArray()) }
        DialogSurface(
            title = stringResource(R.string.select_weekdays),
            onDismiss = ::dismiss,
            buttons = {
                DialogButton(stringResource(android.R.string.cancel), ::dismiss)
                DialogButton(stringResource(android.R.string.yes)) {
                    listener?.onWeekdaysSet(WeekdayList(selectedDays))
                    dismiss()
                }
            }
        ) {
            names.forEachIndexed { index, name ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Checkbox) {
                            checked[index] = !checked[index]
                            selectedDays = toggleDay(selectedDays, index, checked[index])
                        }
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    Checkbox(checked = checked[index], onCheckedChange = null)
                    Text(
                        name,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }

    fun setListener(listener: OnWeekdaysPickedListener?) {
        this.listener = listener
    }

    fun setSelectedDays(days: WeekdayList) {
        selectedDays = days.toArray()
    }

    fun interface OnWeekdaysPickedListener {
        fun onWeekdaysSet(days: WeekdayList)
    }

    companion object {
        private const val KEY_SELECTED_DAYS = "selectedDays"
    }
}
