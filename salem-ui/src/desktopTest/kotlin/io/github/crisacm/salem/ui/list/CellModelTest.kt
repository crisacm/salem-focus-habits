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

package io.github.crisacm.salem.ui.list

import io.github.crisacm.salem.core.models.Entry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class CellModelTest {
    private lateinit var previousLocale: Locale

    // The short form of a number uses the decimal separator of the locale.
    @Before
    fun pinLocale() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun restoreLocale() = Locale.setDefault(previousLocale)

    @Test
    fun yesIsAHabitColouredCheck() {
        val look = checkmarkLook(Entry.YES_MANUAL, questionMarksEnabled = false)
        assertEquals(CheckmarkGlyph.CHECK, look.glyph)
        assertEquals(CellTone.HABIT, look.tone)
        assertEquals(14, look.sizeSp)
        assertFalse(look.hollow)
    }

    @Test
    fun theAutomaticYesIsAHollowAndSlightlySmallerCheck() {
        val look = checkmarkLook(Entry.YES_AUTO, questionMarksEnabled = false)
        assertEquals(CheckmarkGlyph.CHECK, look.glyph)
        assertEquals(CellTone.HABIT, look.tone)
        assertEquals(13, look.sizeSp)
        assertTrue(look.hollow)
    }

    @Test
    fun skipIsAHabitColouredDash() {
        val look = checkmarkLook(Entry.SKIP, questionMarksEnabled = true)
        assertEquals(CheckmarkGlyph.SKIPPED, look.glyph)
        assertEquals(CellTone.HABIT, look.tone)
    }

    @Test
    fun noIsADimTimesUnlessQuestionMarksAreEnabled() {
        assertEquals(CellTone.LOW, checkmarkLook(Entry.NO, questionMarksEnabled = false).tone)
        assertEquals(CellTone.MEDIUM, checkmarkLook(Entry.NO, questionMarksEnabled = true).tone)
        assertEquals(CheckmarkGlyph.TIMES, checkmarkLook(Entry.NO, questionMarksEnabled = true).glyph)
    }

    @Test
    fun unknownIsAQuestionMarkOnlyWhenEnabled() {
        val enabled = checkmarkLook(Entry.UNKNOWN, questionMarksEnabled = true)
        assertEquals(CheckmarkGlyph.QUESTION, enabled.glyph)
        assertEquals(12, enabled.sizeSp)
        assertEquals(CellTone.LOW, enabled.tone)
        val disabled = checkmarkLook(Entry.UNKNOWN, questionMarksEnabled = false)
        assertEquals(CheckmarkGlyph.TIMES, disabled.glyph)
        assertEquals(14, disabled.sizeSp)
    }

    @Test
    fun aNumberIsShownWithItsShortForm() {
        assertEquals(NumberDisplay.Value("4.3k"), numberDisplay(4300.0, questionMarksEnabled = false))
        assertEquals(NumberDisplay.Value("0"), numberDisplay(0.0, questionMarksEnabled = false))
        assertEquals(NumberDisplay.Value("5"), numberDisplay(5.0, questionMarksEnabled = true))
    }

    @Test
    fun skipIsStoredAsThreeThousandths() {
        assertEquals(NumberDisplay.Skipped, numberDisplay(0.003, questionMarksEnabled = false))
    }

    @Test
    fun anUnknownNumberIsAQuestionMarkOrZero() {
        assertEquals(NumberDisplay.Question, numberDisplay(-0.001, questionMarksEnabled = true))
        assertEquals(NumberDisplay.Value("0"), numberDisplay(-0.001, questionMarksEnabled = false))
    }

    @Test
    fun theNumberToneFollowsCompletedAndSign() {
        assertEquals(CellTone.HABIT, numberTone(5.0, completed = true))
        assertEquals(CellTone.HABIT, numberTone(-1.0, completed = true))
        assertEquals(CellTone.LOW, numberTone(-0.001, completed = false))
        assertEquals(CellTone.MEDIUM, numberTone(5.0, completed = false))
        assertEquals(CellTone.MEDIUM, numberTone(0.0, completed = false))
    }

    @Test
    fun theRingIsQuantizedToSixteenSteps() {
        assertEquals(0f, ringSweepAngle(0f), 0.001f)
        assertEquals(360f, ringSweepAngle(1f), 0.001f)
        assertEquals(180f, ringSweepAngle(0.5f), 0.001f)
        assertEquals(22.5f, ringSweepAngle(0.07f), 0.001f)
        assertEquals(0f, ringSweepAngle(0.03f), 0.001f)
    }
}
