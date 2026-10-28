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

package io.github.crisacm.salem.ui.detail

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class ShowHabitModelTest {
    @Before
    fun useEnglishNumbers() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun aYesOrNoHabitShowsTheOverviewAndNotTheTarget() {
        assertEquals(
            listOf(
                ShowCard.SUBTITLE,
                ShowCard.OVERVIEW,
                ShowCard.SCORE,
                ShowCard.BAR,
                ShowCard.FREQUENCY,
                ShowCard.STREAK,
                ShowCard.HISTORY,
                ShowCard.NOTES
            ),
            visibleCards(isNumerical = false, hasNotes = true)
        )
    }

    @Test
    fun aNumericalHabitShowsTheTargetAndNotTheOverview() {
        val cards = visibleCards(isNumerical = true, hasNotes = true)
        assertEquals(true, ShowCard.TARGET in cards)
        assertEquals(false, ShowCard.OVERVIEW in cards)
    }

    @Test
    fun theNotesAreLeftOutWithoutADescription() {
        assertEquals(false, ShowCard.NOTES in visibleCards(isNumerical = false, hasNotes = false))
        assertEquals(ShowCard.SUBTITLE, visibleCards(isNumerical = true, hasNotes = false).first())
    }

    @Test
    fun theTargetTextHasTheShortNumberAndTheUnit() {
        assertEquals("4.3k steps", targetText(4300.0, "steps"))
        assertEquals("30 minutes", targetText(30.0, "minutes"))
    }

    @Test
    fun theIdIsTheLastSegmentOfTheAddress() {
        assertEquals(42L, habitIdFrom("42"))
        assertEquals(null, habitIdFrom(null))
        assertEquals(null, habitIdFrom("habit"))
        assertEquals(null, habitIdFrom(""))
    }
}
