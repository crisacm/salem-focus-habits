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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.ui.compose.DialogSurface

/** A dialog with a list of options; [newInstance] gives the callback the index of the chosen one. */
class ChoiceDialog : ComposeDialogFragment() {
    private var onSelected: (Int) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The callback is not saved: a dialog restored after a rotation could not report anything.
        if (savedInstanceState != null) dismissAllowingStateLoss()
    }

    @Composable
    override fun Content() {
        val options = requireArguments().getStringArrayList(KEY_OPTIONS).orEmpty()
        DialogSurface(title = null, onDismiss = ::dismiss) {
            options.forEachIndexed { index, option ->
                Text(
                    option,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelected(index)
                            dismiss()
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }

    companion object {
        private const val KEY_OPTIONS = "options"

        fun newInstance(options: List<String>, onSelected: (Int) -> Unit) = ChoiceDialog().apply {
            arguments = Bundle().apply { putStringArrayList(KEY_OPTIONS, ArrayList(options)) }
            this.onSelected = onSelected
        }
    }
}
