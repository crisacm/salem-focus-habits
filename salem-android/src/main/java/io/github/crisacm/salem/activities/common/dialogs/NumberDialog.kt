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
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.ui.dialogs.NotesSession
import io.github.crisacm.salem.ui.dialogs.NumberPopup
import io.github.crisacm.salem.ui.dialogs.rememberNumberSheetInfo
import java.text.DecimalFormatSymbols
import java.util.Locale

class NumberDialog : ComposeDialogFragment() {
    var onToggle: (Double, String) -> Unit = { _, _ -> }
    var onDismiss: () -> Unit = {}

    private lateinit var session: NotesSession
    private var originalValue = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = NotesSession(requireArguments().getString("notes")!!)
        originalValue = requireArguments().getDouble("value")
        // The callbacks are not saved: a popup restored after a rotation could not report anything.
        if (savedInstanceState != null) dismissAllowingStateLoss()
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val prefs = remember { (context.applicationContext as HabitsApplication).component.preferences }
        val separator = remember { DecimalFormatSymbols.getInstance().decimalSeparator }
        val args = requireArguments()
        val habit = remember { (context.applicationContext as HabitsApplication).component.habitList.getById(args.getLong("habit")) }
        if (habit == null) {
            // The habit was deleted while the sheet was about to open.
            LaunchedEffect(Unit) { dismissAllowingStateLoss() }
            return
        }
        val formatter = remember { JavaLocalDateFormatter(Locale.getDefault()) }
        val info = rememberNumberSheetInfo(habit, LocalDate(args.getInt("date")), formatter)
        // https://github.com/flutter/flutter/issues/61175
        val textKeyboard = remember { usesTextKeyboardWorkaround(context) }
        NumberPopup(
            session = session,
            info = info,
            originalValue = originalValue,
            skipEnabled = prefs.isSkipEnabled,
            unknownEnabled = prefs.areQuestionMarksEnabled,
            decimalSeparator = separator,
            textKeyboard = textKeyboard,
            onSave = { value, notes ->
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

    private fun usesTextKeyboardWorkaround(context: android.content.Context): Boolean {
        val keyboard = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return keyboard != null && (keyboard.contains("swiftkey") || keyboard.contains("samsung"))
    }
}
