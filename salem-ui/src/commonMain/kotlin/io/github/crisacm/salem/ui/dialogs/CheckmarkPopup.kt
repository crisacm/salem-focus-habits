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

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.platform.gui.FontAwesome
import io.github.crisacm.salem.ui.list.rememberFontAwesome
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.no
import io.github.crisacm.salem.ui.resources.skip_day
import io.github.crisacm.salem.ui.resources.unknown
import io.github.crisacm.salem.ui.resources.yes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The popup of a yes/no cell: a notes field and one button per value of [checkmarkOptions].
 * [onChoose] gets the value and the trimmed notes; the host closes the popup. [session] keeps the notes.
 */
@Composable
fun CheckmarkPopup(
    habitColor: Color,
    session: NotesSession,
    originalValue: Int,
    skipEnabled: Boolean,
    unknownEnabled: Boolean,
    onChoose: (value: Int, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val neutral = MaterialTheme.colorScheme.onSurfaceVariant
    val glyphs = rememberFontAwesome()
    val options = checkmarkOptions(skipEnabled, unknownEnabled)
    PopupCard(onDismiss = onDismiss) {
        NotesField(
            notes = session.notes,
            onChange = { session.notes = it },
            onDone = { onChoose(originalValue, session.choose()) }
        )
        ButtonRow {
            options.forEachIndexed { index, value ->
                if (index > 0) VerticalDivider(modifier = Modifier.fillMaxHeight())
                PopupButton(
                    text = glyphOf(value),
                    color = if (value == Entry.YES_MANUAL || value == Entry.SKIP) habitColor else neutral,
                    description = stringResource(descriptionOf(value)),
                    glyph = glyphs
                ) { onChoose(value, session.choose()) }
            }
        }
    }
}

private fun glyphOf(value: Int) = when (value) {
    Entry.YES_MANUAL -> FontAwesome.CHECK
    Entry.SKIP -> FontAwesome.SKIPPED
    Entry.NO -> FontAwesome.TIMES
    else -> FontAwesome.QUESTION
}

private fun descriptionOf(value: Int): StringResource = when (value) {
    Entry.YES_MANUAL -> Res.string.yes
    Entry.SKIP -> Res.string.skip_day
    Entry.NO -> Res.string.no
    else -> Res.string.unknown
}
