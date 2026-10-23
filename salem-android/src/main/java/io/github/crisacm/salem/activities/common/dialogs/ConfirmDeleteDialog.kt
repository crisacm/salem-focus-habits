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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.ui.callbacks.OnConfirmedCallback
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.ui.compose.DialogButton
import io.github.crisacm.salem.ui.compose.DialogSurface

/**
 * Dialog that asks the user confirmation before executing a delete operation.
 */
class ConfirmDeleteDialog : ComposeDialogFragment() {
    private var callback: OnConfirmedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The callback is not saved: a dialog restored after a rotation could not confirm anything.
        if (savedInstanceState != null) dismissAllowingStateLoss()
    }

    @Composable
    override fun Content() {
        val quantity = requireArguments().getInt(KEY_QUANTITY)
        DialogSurface(
            title = pluralStringResource(R.plurals.delete_habits_title, quantity),
            onDismiss = ::dismiss,
            buttons = {
                DialogButton(stringResource(R.string.no), ::dismiss)
                DialogButton(stringResource(R.string.yes)) {
                    callback?.onConfirmed()
                    dismiss()
                }
            }
        ) {
            Text(
                pluralStringResource(R.plurals.delete_habits_message, quantity),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
    }

    companion object {
        private const val KEY_QUANTITY = "quantity"

        fun newInstance(quantity: Int, callback: OnConfirmedCallback) = ConfirmDeleteDialog().apply {
            arguments = Bundle().apply { putInt(KEY_QUANTITY, quantity) }
            this.callback = callback
        }
    }
}
