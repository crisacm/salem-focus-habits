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

import io.github.crisacm.salem.core.models.Entry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PopupModelsTest {
    @Test
    fun checkmarkOptionsAlwaysHaveYesAndNo() {
        assertEquals(listOf(Entry.YES_MANUAL, Entry.NO), checkmarkOptions(skipEnabled = false, unknownEnabled = false))
    }

    @Test
    fun checkmarkOptionsKeepTheOriginalOrder() {
        assertEquals(
            listOf(Entry.YES_MANUAL, Entry.SKIP, Entry.NO, Entry.UNKNOWN),
            checkmarkOptions(skipEnabled = true, unknownEnabled = true)
        )
        assertEquals(
            listOf(Entry.YES_MANUAL, Entry.SKIP, Entry.NO),
            checkmarkOptions(skipEnabled = true, unknownEnabled = false)
        )
        assertEquals(
            listOf(Entry.YES_MANUAL, Entry.NO, Entry.UNKNOWN),
            checkmarkOptions(skipEnabled = false, unknownEnabled = true)
        )
    }

    @Test
    fun changedNotesAreSavedWhenTheDialogIsClosedWithoutChoosing() {
        assertTrue(shouldSaveNotesOnDismiss(savedViaAction = false, originalNotes = "a", currentNotes = "b"))
    }

    @Test
    fun notesAreNotSavedAgainAfterAnAction() {
        assertFalse(shouldSaveNotesOnDismiss(savedViaAction = true, originalNotes = "a", currentNotes = "b"))
    }

    @Test
    fun unchangedNotesAreIgnoredEvenWithSpaces() {
        assertFalse(shouldSaveNotesOnDismiss(savedViaAction = false, originalNotes = "a", currentNotes = "a"))
        assertFalse(shouldSaveNotesOnDismiss(savedViaAction = false, originalNotes = "a", currentNotes = "  a "))
    }

    @Test
    fun numberFieldShowsZeroForTinyValues() {
        assertEquals("0", formatNumberFieldValue(0.0, '.'))
        assertEquals("0", formatNumberFieldValue(0.009, '.'))
        assertEquals("0", formatNumberFieldValue(-0.001, '.'))
    }

    @Test
    fun numberFieldRoundsToTwoDecimals() {
        assertEquals("1.5", formatNumberFieldValue(1.5, '.'))
        assertEquals("2.35", formatNumberFieldValue(2.347, '.'))
        assertEquals("10", formatNumberFieldValue(10.0, '.'))
    }

    @Test
    fun numberFieldUsesTheDecimalSeparatorOfTheLocale() {
        assertEquals("1,5", formatNumberFieldValue(1.5, ','))
        assertEquals("2,35", formatNumberFieldValue(2.347, ','))
    }

    @Test
    fun specialValuesAreWrittenInTheFieldBeforeSaving() {
        assertEquals("0.003", formatSpecialValue(Entry.SKIP, '.'))
        assertEquals("-0.001", formatSpecialValue(Entry.UNKNOWN, '.'))
        assertEquals("0,003", formatSpecialValue(Entry.SKIP, ','))
    }

    @Test
    fun parsingReadsTheNumber() {
        assertEquals(12.5, parseNumberFieldValue("12.5", original = 3.0, separator = '.'), 0.0)
        assertEquals(0.003, parseNumberFieldValue("0.003", original = 3.0, separator = '.'), 0.0)
        assertEquals(12.5, parseNumberFieldValue("12,5", original = 3.0, separator = ','), 0.0)
    }

    @Test
    fun emptyTextMeansUnknown() {
        assertEquals(-0.001, parseNumberFieldValue("", original = 3.0, separator = '.'), 0.0)
    }

    @Test
    fun textThatCannotBeParsedKeepsTheOriginalValue() {
        assertEquals(7.0, parseNumberFieldValue("abc", original = 7.0, separator = '.'), 0.0)
        assertEquals(7.0, parseNumberFieldValue(".", original = 7.0, separator = '.'), 0.0)
    }

    @Test
    fun inputOnlyAllowsDigitsAndTheSeparator() {
        assertTrue(isAllowedNumberInput("12.5", '.'))
        assertTrue(isAllowedNumberInput("", '.'))
        assertTrue(isAllowedNumberInput("12,5", ','))
        assertFalse(isAllowedNumberInput("12,5", '.'))
        assertFalse(isAllowedNumberInput("1a", '.'))
        assertFalse(isAllowedNumberInput("-1", '.'))
    }

    @Test
    fun aSessionStartsWithTheOriginalNotesAndNothingToSave() {
        val session = NotesSession("a")
        assertEquals("a", session.notes)
        assertNull(session.pendingNotesOnDismiss())
    }

    @Test
    fun aSessionSavesChangedNotesWhenClosedWithoutChoosing() {
        val session = NotesSession("a")
        session.notes = " b "
        assertEquals("b", session.pendingNotesOnDismiss())
    }

    @Test
    fun choosingAnActionReturnsTrimmedNotesAndStopsTheDismissSave() {
        val session = NotesSession("a")
        session.notes = " b "
        assertEquals("b", session.choose())
        assertNull(session.pendingNotesOnDismiss())
    }
}
