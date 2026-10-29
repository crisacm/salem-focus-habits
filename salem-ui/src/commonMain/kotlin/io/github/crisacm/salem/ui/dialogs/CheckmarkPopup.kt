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
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.ui.edit.ScallopShape
import io.github.crisacm.salem.ui.edit.isDarkScheme
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.no
import io.github.crisacm.salem.ui.resources.notes
import io.github.crisacm.salem.ui.resources.skip_day
import io.github.crisacm.salem.ui.resources.unknown
import io.github.crisacm.salem.ui.resources.yes
import org.jetbrains.compose.resources.stringResource

/**
 * The sheet of a yes or no cell: the notes and a yes and a no button, with Skip and Unknown under them when they are
 * on. [onChoose] gets the value and the trimmed notes; the host closes the sheet. [session] keeps the notes.
 */
@Composable
fun CheckmarkPopup(
    session: NotesSession,
    info: NumberSheetInfo,
    originalValue: Int,
    skipEnabled: Boolean,
    unknownEnabled: Boolean,
    onChoose: (value: Int, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(28.dp)
    val yes = stringResource(Res.string.yes)
    val no = stringResource(Res.string.no)
    SalemBottomSheet(onDismiss) {
        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(ScallopShape()).background(info.accent), contentAlignment = Alignment.Center) {
                Icon(info.icon, contentDescription = null, tint = info.onAccent, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
                Text(
                    stringResource(Res.string.notes),
                    color = scheme.onSurface,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${info.title} · ${info.subtitle}",
                    color = scheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SheetCloseButton(onDismiss)
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp)) {
            TextField(
                value = session.notes,
                onValueChange = { session.notes = it },
                placeholder = { Text(stringResource(Res.string.notes), fontSize = 16.sp) },
                textStyle = TextStyle(fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onChoose(originalValue, session.choose()) }),
                shape = shape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = sheetCardColor(),
                    unfocusedContainerColor = sheetCardColor(),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = info.accent
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = 148.dp)
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ChoicePill(Icons.Filled.Check, yes, info.accent, info.onAccent, Modifier.weight(1f)) {
                    onChoose(Entry.YES_MANUAL, session.choose())
                }
                ChoicePill(Icons.Filled.Close, no, closeButtonColor(), scheme.onSurface, Modifier.weight(1f)) {
                    onChoose(Entry.NO, session.choose())
                }
            }
            if (skipEnabled || unknownEnabled) {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
                    if (skipEnabled) {
                        val label = stringResource(Res.string.skip_day)
                        TextButton(
                            onClick = { onChoose(Entry.SKIP, session.choose()) },
                            modifier = Modifier.semantics { contentDescription = label }
                        ) { Text(label, color = scheme.onSurfaceVariant) }
                    }
                    if (unknownEnabled) {
                        val label = stringResource(Res.string.unknown)
                        TextButton(
                            onClick = { onChoose(Entry.UNKNOWN, session.choose()) },
                            modifier = Modifier.semantics { contentDescription = label }
                        ) { Text(label, color = scheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}

/** A wide round button with an icon, as the yes and the no of the sheet. */
@Composable
private fun ChoicePill(icon: ImageVector, description: String, container: Color, content: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(64.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(28.dp))
    }
}

/** The grey of the round buttons of a sheet. */
@Composable
internal fun closeButtonColor(): Color = if (isDarkScheme()) Color(0xFF36343B) else Color(0xFFE3E0E9)
