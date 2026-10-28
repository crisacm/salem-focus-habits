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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitTonesTest {
    private val blue = Color(0xFF1976D2)

    @Test
    fun theLightCardIsPaleAndTheAccentIsStrong() {
        val tones = habitTones(blue, dark = false)
        assertTrue(tones.container.luminance() > 0.7f)
        assertTrue(tones.accent.luminance() < 0.25f)
        assertEquals(Color.White, tones.onAccent)
    }

    @Test
    fun theDarkCardIsDeepAndTheAccentIsPale() {
        val tones = habitTones(blue, dark = true)
        assertTrue(tones.container.luminance() < 0.15f)
        assertTrue(tones.accent.luminance() > 0.4f)
        assertTrue(tones.onAccent.luminance() < 0.1f)
    }

    @Test
    fun theTonesKeepTheHueOfTheHabit() {
        // Blue stays blue: the blue channel dominates in every tone of the card
        val tones = habitTones(blue, dark = false)
        for (tone in listOf(tones.container, tones.accent, tones.onContainer)) assertTrue(tone.blue >= tone.red)
    }

    @Test
    fun aGreyHabitGivesGreyTones() {
        val tones = habitTones(Color(0xFF9E9E9E), dark = false)
        for (tone in listOf(tones.container, tones.accent, tones.soft)) {
            assertEquals(tone.red, tone.green, 0.01f)
            assertEquals(tone.green, tone.blue, 0.01f)
        }
    }
}
