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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.edit.ScallopShape
import io.github.crisacm.salem.ui.edit.isDarkScheme
import io.github.crisacm.salem.ui.list.habitIconFor
import io.github.crisacm.salem.ui.list.habitTones
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.notes
import io.github.crisacm.salem.ui.resources.save
import io.github.crisacm.salem.ui.resources.skip_day
import io.github.crisacm.salem.ui.resources.unknown
import io.github.crisacm.salem.ui.resources.value
import org.jetbrains.compose.resources.stringResource

/** What the sheet of a number shows about the habit and the day: its name, the day, the unit and the colours and icon of the habit. */
class NumberSheetInfo(
    val title: String,
    val subtitle: String,
    val unit: String,
    val accent: Color,
    val onAccent: Color,
    val icon: ImageVector
)

/** The [NumberSheetInfo] of [habit] on [date], with the tones of the scheme that is shown. */
@Composable
fun rememberNumberSheetInfo(habit: Habit, date: LocalDate, dateFormatter: LocalDateFormatter): NumberSheetInfo {
    val dark = isDarkScheme()
    return remember(habit, date, dark) {
        val tones = habitTones(habit.color.paletteIndex, dark)
        NumberSheetInfo(
            title = habit.name,
            subtitle = "${dateFormatter.longWeekdayName(date.dayOfWeek)}, ${dateFormatter.shortMonthName(date)} ${date.day}",
            unit = habit.unit,
            accent = tones.accent,
            onAccent = tones.onAccent,
            icon = habitIconFor(habit.uuid, habit.id)
        )
    }
}

/**
 * The sheet of a numerical cell: the habit and the day, the number with its unit, the notes and a Save button, with
 * Skip and Unknown under it when they are on. [onSave] gets the number and the trimmed notes; the host closes the
 * sheet. [decimalSeparator] is the one of the user's locale and [textKeyboard] asks for a plain text keyboard, for the
 * Android keyboards that do not offer the decimal mark on the numeric one.
 */
@Composable
fun NumberPopup(
    session: NotesSession,
    info: NumberSheetInfo,
    originalValue: Double,
    skipEnabled: Boolean,
    unknownEnabled: Boolean,
    decimalSeparator: Char,
    textKeyboard: Boolean,
    onSave: (value: Double, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val initial = formatNumberFieldValue(originalValue, decimalSeparator)
    var value by remember { mutableStateOf(TextFieldValue(initial, TextRange(0, initial.length))) }
    var focused by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        // The field may not be attached yet on the first frame; the user can still tap it.
        runCatching { focus.requestFocus() }
        keyboard?.show()
    }
    fun save(text: String) = onSave(parseNumberFieldValue(text, originalValue, decimalSeparator), session.choose())

    val shape = RoundedCornerShape(28.dp)
    val card = sheetCardColor()
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = card,
        unfocusedContainerColor = card,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedLabelColor = info.accent,
        unfocusedLabelColor = scheme.onSurfaceVariant,
        cursorColor = info.accent
    )
    val saveLabel = stringResource(Res.string.save)
    SalemBottomSheet(onDismiss) {
        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(ScallopShape()).background(info.accent), contentAlignment = Alignment.Center) {
                Icon(info.icon, contentDescription = null, tint = info.onAccent, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
                Text(
                    info.title,
                    color = scheme.onSurface,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(info.subtitle, color = scheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
            }
            SheetCloseButton(onDismiss)
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 28.dp)) {
            TextField(
                value = value,
                onValueChange = { if (isAllowedNumberInput(it.text, decimalSeparator)) value = it },
                label = { Text(stringResource(Res.string.value)) },
                suffix = {
                    if (info.unit.isNotBlank()) Text(info.unit, color = scheme.onSurfaceVariant, fontSize = 16.sp)
                },
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (textKeyboard) KeyboardType.Text else KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { save(value.text) }),
                shape = shape,
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (focused) Modifier.border(2.dp, info.accent, shape) else Modifier)
                    .focusRequester(focus)
                    .onFocusChanged { focused = it.isFocused }
            )
            Spacer(Modifier.height(12.dp))
            TextField(
                value = session.notes,
                onValueChange = { session.notes = it },
                placeholder = { Text(stringResource(Res.string.notes), fontSize = 16.sp) },
                textStyle = TextStyle(fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save(value.text) }),
                shape = shape,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { save(value.text) },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = info.accent, contentColor = info.onAccent),
                modifier = Modifier.fillMaxWidth().height(60.dp).semantics { contentDescription = saveLabel }
            ) {
                Text(saveLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            if (skipEnabled || unknownEnabled) {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
                    if (skipEnabled) {
                        val label = stringResource(Res.string.skip_day)
                        TextButton(
                            onClick = { save(formatSpecialValue(Entry.SKIP, decimalSeparator)) },
                            modifier = Modifier.semantics { contentDescription = label }
                        ) { Text(label, color = scheme.onSurfaceVariant) }
                    }
                    if (unknownEnabled) {
                        val label = stringResource(Res.string.unknown)
                        TextButton(
                            onClick = { save(formatSpecialValue(Entry.UNKNOWN, decimalSeparator)) },
                            modifier = Modifier.semantics { contentDescription = label }
                        ) { Text(label, color = scheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}
