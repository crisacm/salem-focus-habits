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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.platform.io.formatInvariant

/** Values offered by the checkmark popup, in the order they are shown. */
fun checkmarkOptions(skipEnabled: Boolean, unknownEnabled: Boolean): List<Int> = buildList {
    add(Entry.YES_MANUAL)
    if (skipEnabled) add(Entry.SKIP)
    add(Entry.NO)
    if (unknownEnabled) add(Entry.UNKNOWN)
}

/** Closing a popup without choosing still saves the notes, with the original value, if they changed. */
fun shouldSaveNotesOnDismiss(savedViaAction: Boolean, originalNotes: String, currentNotes: String) =
    !savedViaAction && currentNotes.trim() != originalNotes.trim()

/**
 * The notes of a checkmark or number popup. The popup and its host share it: the host asks for
 * [pendingNotesOnDismiss] when the popup is closed by any means other than a button.
 */
class NotesSession(private val originalNotes: String) {
    var notes by mutableStateOf(originalNotes)
    private var savedViaAction = false

    /** Marks the popup as answered by a button and returns the notes to save with the answer. */
    fun choose(): String {
        savedViaAction = true
        return notes.trim()
    }

    /** The notes to save with the original value, or null if the popup was answered or nothing changed. */
    fun pendingNotesOnDismiss(): String? =
        if (shouldSaveNotesOnDismiss(savedViaAction, originalNotes, notes)) notes.trim() else null
}

/** [value] with at most [maxDecimals] decimals, without trailing zeros and with [separator] as decimal mark. */
private fun trimmed(value: Double, maxDecimals: Int, separator: Char): String {
    val text = formatInvariant("%.${maxDecimals}f", value)
    val short = if ('.' in text) text.trimEnd('0').trimEnd('.') else text
    return short.replace('.', separator)
}

fun formatNumberFieldValue(value: Double, separator: Char): String =
    if (value < 0.01) "0" else trimmed(value, 2, separator)

/** The text the Skip and Unknown buttons write in the number field; values are stored in thousandths. */
fun formatSpecialValue(entryValue: Int, separator: Char): String = trimmed(entryValue.toDouble() / 1000, 3, separator)

/** Empty text means unknown; text that cannot be parsed keeps the [original] value. */
fun parseNumberFieldValue(text: String, original: Double, separator: Char): Double {
    if (text.isEmpty()) return Entry.UNKNOWN.toDouble() / 1000
    return text.replace(separator, '.').toDoubleOrNull() ?: original
}

/** Only ASCII digits and at most one [separator]: anything else could not be parsed and would silently keep the old value. */
fun isAllowedNumberInput(text: String, separator: Char) =
    text.all { it in '0'..'9' || it == separator } && text.count { it == separator } <= 1
