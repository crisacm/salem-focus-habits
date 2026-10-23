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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.utils.InterfaceUtils.getFontAwesome

class CheckmarkDialog : ComposeDialogFragment() {
    var onToggle: (Int, String) -> Unit = { _, _ -> }
    var onDismiss: () -> Unit = {}

    private var savedViaAction = false
    private var originalNotes = ""
    private var originalValue = 0
    private var currentNotes = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalNotes = requireArguments().getString("notes")!!
        originalValue = requireArguments().getInt("value")
        currentNotes = originalNotes
        // The callbacks are not saved: a popup restored after a rotation could not report anything.
        if (savedInstanceState != null) dismissAllowingStateLoss()
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val prefs = remember { (context.applicationContext as HabitsApplication).component.preferences }
        val habitColor = Color(requireArguments().getInt("color"))
        val neutral = MaterialTheme.colorScheme.onSurfaceVariant
        val glyphs = remember { getFontAwesome(context)?.let { FontFamily(it) } }
        var notes by remember { mutableStateOf(originalNotes) }
        val options = checkmarkOptions(prefs.isSkipEnabled, prefs.areQuestionMarksEnabled)

        PopupCard(onDismiss = ::dismiss) {
            NotesField(
                notes = notes,
                onChange = {
                    notes = it
                    currentNotes = it
                },
                onDone = { choose(originalValue) }
            )
            ButtonRow {
                options.forEachIndexed { index, value ->
                    if (index > 0) VerticalDivider(modifier = Modifier.fillMaxHeight().padding(vertical = 0.dp))
                    PopupButton(
                        text = stringResource(glyphOf(value)),
                        color = if (value == Entry.YES_MANUAL || value == Entry.SKIP) habitColor else neutral,
                        description = stringResource(descriptionOf(value)),
                        glyph = glyphs
                    ) { choose(value) }
                }
            }
        }
    }

    private fun choose(value: Int) {
        savedViaAction = true
        onToggle(value, currentNotes.trim())
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (shouldSaveNotesOnDismiss(savedViaAction, originalNotes, currentNotes)) {
            onToggle(originalValue, currentNotes.trim())
        }
        onDismiss()
    }

    private fun glyphOf(value: Int) = when (value) {
        Entry.YES_MANUAL -> R.string.fa_check
        Entry.SKIP -> R.string.fa_skipped
        Entry.NO -> R.string.fa_times
        else -> R.string.fa_question
    }

    private fun descriptionOf(value: Int) = when (value) {
        Entry.YES_MANUAL -> R.string.yes
        Entry.SKIP -> R.string.skip_day
        Entry.NO -> R.string.no
        else -> R.string.unknown
    }
}
