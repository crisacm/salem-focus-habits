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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.utils.InterfaceUtils.getFontAwesome
import java.text.DecimalFormatSymbols

class NumberDialog : ComposeDialogFragment() {
    var onToggle: (Double, String) -> Unit = { _, _ -> }
    var onDismiss: () -> Unit = {}

    private var savedViaAction = false
    private var originalNotes = ""
    private var originalValue = 0.0
    private var currentNotes = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalNotes = requireArguments().getString("notes")!!
        originalValue = requireArguments().getDouble("value")
        currentNotes = originalNotes
        // The callbacks are not saved: a popup restored after a rotation could not report anything.
        if (savedInstanceState != null) dismissAllowingStateLoss()
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val prefs = remember { (context.applicationContext as HabitsApplication).component.preferences }
        val neutral = MaterialTheme.colorScheme.onSurfaceVariant
        val glyphs = remember { getFontAwesome(context)?.let { FontFamily(it) } }
        val separator = remember { DecimalFormatSymbols.getInstance().decimalSeparator }
        // https://github.com/flutter/flutter/issues/61175
        val textKeyboard = remember { usesTextKeyboardWorkaround(context) }
        var notes by remember { mutableStateOf(originalNotes) }
        val initial = formatNumberFieldValue(originalValue)
        var value by remember { mutableStateOf(TextFieldValue(initial, TextRange(0, initial.length))) }
        val focus = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current
        LaunchedEffect(Unit) {
            // The field may not be attached yet on the first frame; the user can still tap it.
            runCatching { focus.requestFocus() }
            keyboard?.show()
        }

        PopupCard(onDismiss = ::dismiss) {
            NotesField(
                notes = notes,
                onChange = {
                    notes = it
                    currentNotes = it
                },
                onDone = { save(value.text) }
            )
            ButtonRow {
                TextField(
                    value = value,
                    onValueChange = { if (isAllowedNumberInput(it.text, separator)) value = it },
                    singleLine = true,
                    textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (textKeyboard) KeyboardType.Text else KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { save(value.text) }),
                    colors = transparentFieldColors(),
                    modifier = Modifier.weight(2f).focusRequester(focus)
                )
                PopupButton(stringResource(R.string.save), MaterialTheme.colorScheme.primary, stringResource(R.string.save)) {
                    save(value.text)
                }
                if (prefs.isSkipEnabled) {
                    PopupButton(stringResource(R.string.skip_day), MaterialTheme.colorScheme.primary, stringResource(R.string.skip_day)) {
                        save(formatSpecialValue(Entry.SKIP))
                    }
                }
                if (prefs.areQuestionMarksEnabled) {
                    PopupButton(
                        stringResource(R.string.fa_question),
                        neutral,
                        stringResource(R.string.unknown),
                        glyph = glyphs
                    ) { save(formatSpecialValue(Entry.UNKNOWN)) }
                }
            }
        }
    }

    private fun save(text: String) {
        savedViaAction = true
        onToggle(parseNumberFieldValue(text, originalValue), currentNotes.trim())
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (shouldSaveNotesOnDismiss(savedViaAction, originalNotes, currentNotes)) {
            onToggle(originalValue, currentNotes.trim())
        }
        onDismiss()
    }

    private fun usesTextKeyboardWorkaround(context: android.content.Context): Boolean {
        val keyboard = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return keyboard != null && (keyboard.contains("swiftkey") || keyboard.contains("samsung"))
    }
}
