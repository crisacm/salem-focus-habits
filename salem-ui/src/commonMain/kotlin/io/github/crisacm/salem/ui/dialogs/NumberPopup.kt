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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.platform.gui.FontAwesome
import io.github.crisacm.salem.ui.list.rememberFontAwesome
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.save
import io.github.crisacm.salem.ui.resources.skip_day
import io.github.crisacm.salem.ui.resources.unknown
import org.jetbrains.compose.resources.stringResource

/**
 * The popup of a numerical cell: a notes field, the number and the Save, Skip and Unknown buttons.
 * [onSave] gets the number and the trimmed notes; the host closes the popup. [decimalSeparator] is the one of
 * the user's locale and [textKeyboard] asks for a plain text keyboard, for the Android keyboards that
 * do not offer the decimal mark on the numeric one.
 */
@Composable
fun NumberPopup(
    session: NotesSession,
    originalValue: Double,
    skipEnabled: Boolean,
    unknownEnabled: Boolean,
    decimalSeparator: Char,
    textKeyboard: Boolean,
    onSave: (value: Double, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val neutral = MaterialTheme.colorScheme.onSurfaceVariant
    val glyphs = rememberFontAwesome()
    val initial = formatNumberFieldValue(originalValue, decimalSeparator)
    var value by remember { mutableStateOf(TextFieldValue(initial, TextRange(0, initial.length))) }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        // The field may not be attached yet on the first frame; the user can still tap it.
        runCatching { focus.requestFocus() }
        keyboard?.show()
    }
    fun save(text: String) = onSave(parseNumberFieldValue(text, originalValue, decimalSeparator), session.choose())

    PopupCard(onDismiss = onDismiss) {
        NotesField(notes = session.notes, onChange = { session.notes = it }, onDone = { save(value.text) })
        ButtonRow {
            TextField(
                value = value,
                onValueChange = { if (isAllowedNumberInput(it.text, decimalSeparator)) value = it },
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
            PopupButton(stringResource(Res.string.save), MaterialTheme.colorScheme.primary, stringResource(Res.string.save)) {
                save(value.text)
            }
            if (skipEnabled) {
                PopupButton(
                    stringResource(Res.string.skip_day),
                    MaterialTheme.colorScheme.primary,
                    stringResource(Res.string.skip_day)
                ) { save(formatSpecialValue(Entry.SKIP, decimalSeparator)) }
            }
            if (unknownEnabled) {
                PopupButton(FontAwesome.QUESTION, neutral, stringResource(Res.string.unknown), glyph = glyphs) {
                    save(formatSpecialValue(Entry.UNKNOWN, decimalSeparator))
                }
            }
        }
    }
}
