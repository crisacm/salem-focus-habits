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
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitTonesTest {
    private fun argb(hex: Long) = Color(0xFF000000 or hex)

    @Test
    fun theBlueOfTheDesignHasItsMeasuredTonesInTheLightTheme() {
        val tones = habitTones(11, dark = false)
        assertEquals(argb(0xDDE8FB), tones.container)
        assertEquals(argb(0x0A2958), tones.onContainer)
        assertEquals(argb(0x1B5FC1), tones.accent)
        assertEquals(argb(0xFFFFFF), tones.onAccent)
        assertEquals(argb(0xF5F8FE), tones.soft)
    }

    @Test
    fun theBlueOfTheDesignHasItsMeasuredTonesInTheDarkTheme() {
        val tones = habitTones(11, dark = true)
        assertEquals(argb(0x1C3460), tones.container)
        assertEquals(argb(0xDBEEFF), tones.onContainer)
        assertEquals(argb(0xAAC7FF), tones.accent)
        assertEquals(argb(0x012C62), tones.onAccent)
        assertEquals(argb(0x132340), tones.soft)
    }

    @Test
    fun thePinkOrangeAndPurpleOfTheDesignHaveTheirAccent() {
        assertEquals(argb(0xB5135A), habitTones(15, dark = false).accent)
        assertEquals(argb(0xFFB2CC), habitTones(15, dark = true).accent)
        assertEquals(argb(0xA95500), habitTones(2, dark = false).accent)
        assertEquals(argb(0xFFB86C), habitTones(2, dark = true).accent)
        assertEquals(argb(0x6A40B4), habitTones(13, dark = false).accent)
        assertEquals(argb(0xD0BCFF), habitTones(13, dark = true).accent)
    }

    @Test
    fun theRingTrackIsTheSoftTone() {
        for (index in listOf(11, 8)) for (dark in listOf(false, true)) {
            val tones = habitTones(index, dark)
            assertEquals(tones.soft, tones.ringTrack)
        }
    }

    @Test
    fun theTextOfAnEmptyDayIsBetweenTheSoftToneAndTheTextOfTheCard() {
        val tones = habitTones(11, dark = true)
        assertTrue(tones.onSoft.luminance() > tones.soft.luminance())
        assertTrue(tones.onSoft.luminance() < tones.onContainer.luminance())
    }

    @Test
    fun aColourWithoutAFamilyGetsTonesInTheSameBands() {
        // Teal, green, red and brown are not in the design
        for (index in listOf(8, 7, 0, 16)) {
            val light = habitTones(index, dark = false)
            assertTrue(light.container.luminance() > 0.7f, "container of $index")
            assertTrue(light.accent.luminance() < 0.3f, "accent of $index")
            assertTrue(light.soft.luminance() > light.container.luminance(), "soft of $index")
            val dark = habitTones(index, dark = true)
            assertTrue(dark.container.luminance() < 0.1f, "dark container of $index")
            assertTrue(dark.accent.luminance() > 0.4f, "dark accent of $index")
            assertTrue(dark.onAccent.luminance() < 0.05f, "dark onAccent of $index")
        }
    }

    @Test
    fun theTonesOfAColourAreAlwaysTheSame() {
        val first = habitTones(8, dark = false)
        val second = habitTones(8, dark = false)
        assertEquals(first.container, second.container)
        assertEquals(first.accent, second.accent)
    }

    @Test
    fun theColourOfAnArchivedHabitIsGrey() {
        for (dark in listOf(false, true)) {
            val tones = habitTones(ARCHIVED_TONES, dark)
            for (tone in listOf(tones.container, tones.accent, tones.soft, tones.onContainer)) {
                assertEquals(tone.red, tone.green, 0.01f)
                assertEquals(tone.green, tone.blue, 0.01f)
            }
        }
    }

    @Test
    fun theMutedAndEmphasisTonesAreBetweenTheOthers() {
        for (dark in listOf(false, true)) {
            val tones = habitTones(15, dark)
            // The muted colour is a step from the card towards the accent, the emphasis one from the accent to the text
            assertTrue(tones.muted != tones.container && tones.muted != tones.accent)
            assertTrue(tones.emphasis != tones.accent && tones.emphasis != tones.onContainer)
        }
    }

    @Test
    fun theEmphasisOfThePinkMatchesTheMockups() {
        assertEquals(0xFFBCD6, packed(habitTones(15, dark = true).emphasis))
    }

    private fun packed(color: Color) =
        ((color.red * 255).roundToInt() shl 16) or ((color.green * 255).roundToInt() shl 8) or (color.blue * 255).roundToInt()
}
