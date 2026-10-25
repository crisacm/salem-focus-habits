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

import android.content.DialogInterface
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.ui.dialogs.CheckmarkPopup
import io.github.crisacm.salem.ui.dialogs.NotesSession

class CheckmarkDialog : ComposeDialogFragment() {
    var onToggle: (Int, String) -> Unit = { _, _ -> }
    var onDismiss: () -> Unit = {}

    private lateinit var session: NotesSession
    private var originalValue = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = NotesSession(requireArguments().getString("notes")!!)
        originalValue = requireArguments().getInt("value")
        // The callbacks are not saved: a popup restored after a rotation could not report anything.
        if (savedInstanceState != null) dismissAllowingStateLoss()
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val prefs = remember { (context.applicationContext as HabitsApplication).component.preferences }
        CheckmarkPopup(
            habitColor = Color(requireArguments().getInt("color")),
            session = session,
            originalValue = originalValue,
            skipEnabled = prefs.isSkipEnabled,
            unknownEnabled = prefs.areQuestionMarksEnabled,
            onChoose = { value, notes ->
                onToggle(value, notes)
                dismiss()
            },
            onDismiss = ::dismiss
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        session.pendingNotesOnDismiss()?.let { onToggle(originalValue, it) }
        onDismiss()
    }
}
