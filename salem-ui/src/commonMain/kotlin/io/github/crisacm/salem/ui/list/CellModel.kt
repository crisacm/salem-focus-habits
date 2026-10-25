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
import io.github.crisacm.salem.core.ui.views.toShortString
import kotlin.math.round

/** Which of the colours of a cell is used: the colour of the habit, a dim one or a medium one. */
enum class CellTone { HABIT, LOW, MEDIUM }

enum class CheckmarkGlyph { CHECK, TIMES, SKIPPED, QUESTION }

class CheckmarkLook(val glyph: CheckmarkGlyph, val tone: CellTone, val sizeSp: Int, val hollow: Boolean)

/** How a checkmark cell looks for an entry [value]. */
fun checkmarkLook(value: Int, questionMarksEnabled: Boolean): CheckmarkLook {
    val tone = when (value) {
        Entry.YES_MANUAL, Entry.YES_AUTO, Entry.SKIP -> CellTone.HABIT
        Entry.NO -> if (questionMarksEnabled) CellTone.MEDIUM else CellTone.LOW
        else -> CellTone.LOW
    }
    val glyph = when (value) {
        Entry.SKIP -> CheckmarkGlyph.SKIPPED
        Entry.NO -> CheckmarkGlyph.TIMES
        Entry.UNKNOWN -> if (questionMarksEnabled) CheckmarkGlyph.QUESTION else CheckmarkGlyph.TIMES
        else -> CheckmarkGlyph.CHECK
    }
    val size = when {
        glyph == CheckmarkGlyph.QUESTION -> 12
        value == Entry.YES_AUTO -> 13
        else -> 14
    }
    return CheckmarkLook(glyph, tone, size, hollow = value == Entry.YES_AUTO)
}

sealed class NumberDisplay {
    data object Skipped : NumberDisplay()

    data object Question : NumberDisplay()

    data class Value(val text: String) : NumberDisplay()
}

/** What a number cell shows; entry values are stored in thousandths, so skip is 0.003 and unknown is -0.001. */
fun numberDisplay(value: Double, questionMarksEnabled: Boolean): NumberDisplay = when {
    value == Entry.SKIP.toDouble() / 1000 -> NumberDisplay.Skipped
    value >= 0 -> NumberDisplay.Value(value.toShortString())
    questionMarksEnabled -> NumberDisplay.Question
    else -> NumberDisplay.Value("0")
}

fun numberTone(value: Double, completed: Boolean) = when {
    completed -> CellTone.HABIT
    value < 0 -> CellTone.LOW
    else -> CellTone.MEDIUM
}

const val RING_PRECISION = 1f / 16

/** The angle of the filled part of the score ring, in steps of a sixteenth of a turn. */
fun ringSweepAngle(percentage: Float, precision: Float = RING_PRECISION): Float =
    360f * round(percentage / precision) * precision
