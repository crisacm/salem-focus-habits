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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.ui.dialogs.WeekdayPickerContent
import io.github.crisacm.salem.ui.dialogs.weekdayNames
import java.util.Locale

/**
 * Dialog that allows the user to pick one or more days of the week.
 */
class WeekdayPickerDialog : ComposeDialogFragment() {
    private var selectedDays = WeekdayList.EVERY_DAY
    private var listener: OnWeekdaysPickedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The listener is not saved: a dialog restored after a rotation could not report anything.
        if (savedInstanceState != null) dismissAllowingStateLoss()
    }

    @Composable
    override fun Content() {
        val names = remember { weekdayNames(JavaLocalDateFormatter(Locale.getDefault())) }
        WeekdayPickerContent(
            dayNames = names,
            selected = selectedDays,
            onPicked = {
                listener?.onWeekdaysSet(it)
                dismiss()
            },
            onDismiss = ::dismiss,
            confirmLabel = stringResource(android.R.string.yes),
            cancelLabel = stringResource(android.R.string.cancel)
        )
    }

    fun setListener(listener: OnWeekdaysPickedListener?) {
        this.listener = listener
    }

    fun setSelectedDays(days: WeekdayList) {
        selectedDays = days
    }

    fun interface OnWeekdaysPickedListener {
        fun onWeekdaysSet(days: WeekdayList)
    }
}
