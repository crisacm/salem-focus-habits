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

import io.github.crisacm.salem.core.models.Entry
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException

/** Values offered by the checkmark popup, in the order they are shown. */
fun checkmarkOptions(skipEnabled: Boolean, unknownEnabled: Boolean): List<Int> = buildList {
    add(Entry.YES_MANUAL)
    if (skipEnabled) add(Entry.SKIP)
    add(Entry.NO)
    if (unknownEnabled) add(Entry.UNKNOWN)
}

/** Closing a popup without choosing still saves the notes, with the original value, if they changed. */
fun shouldSaveNotesOnDismiss(savedViaAction: Boolean, originalNotes: String, currentNotes: String) =
    !savedViaAction && currentNotes.trim() != originalNotes

fun formatNumberFieldValue(value: Double): String =
    if (value < 0.01) "0" else DecimalFormat("#.##").format(value)

/** The text the Skip and Unknown buttons write in the number field; values are stored in thousandths. */
fun formatSpecialValue(entryValue: Int): String = DecimalFormat("#.###").format(entryValue.toDouble() / 1000)

/** Empty text means unknown; text that cannot be parsed keeps the [original] value. */
fun parseNumberFieldValue(
    text: String,
    original: Double,
    parse: (String) -> Double = { NumberFormat.getInstance().parse(it)!!.toDouble() }
): Double {
    if (text.isEmpty()) return Entry.UNKNOWN.toDouble() / 1000
    return try {
        parse(text)
    } catch (e: ParseException) {
        original
    }
}

fun isAllowedNumberInput(text: String, separator: Char) = text.all { it.isDigit() || it == separator }
