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

import io.github.crisacm.salem.platform.gui.FontAwesome
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** The glyphs of the core `FontAwesome` are the ones of `fontawesome.xml`, which the Android Views still read. */
class FontAwesomeGlyphsTest {
    private val xml = File("../salem-android/src/main/res/values/fontawesome.xml").readText()

    private fun glyph(name: String): String {
        val code = Regex("""<string[^>]*name="$name"[^>]*>&#x([0-9a-f]+);</string>""").find(xml)!!.groupValues[1]
        return String(Character.toChars(code.toInt(16)))
    }

    @Test
    fun theConstantsMatchTheAndroidResources() {
        assertEquals(glyph("fa_check"), FontAwesome.CHECK)
        assertEquals(glyph("fa_times"), FontAwesome.TIMES)
        assertEquals(glyph("fa_skipped"), FontAwesome.SKIPPED)
        assertEquals(glyph("fa_question"), FontAwesome.QUESTION)
        assertEquals(glyph("fa_star_half_o"), FontAwesome.STAR_HALF_O)
        assertEquals(glyph("fa_umbrella_beach"), FontAwesome.UMBRELLA_BEACH)
        assertEquals(glyph("fa_calendar"), FontAwesome.CALENDAR)
        assertEquals(glyph("fa_bell_o"), FontAwesome.BELL_O)
        assertEquals(glyph("fa_arrow_circle_up"), FontAwesome.ARROW_CIRCLE_UP)
        assertEquals(glyph("fa_arrow_circle_down"), FontAwesome.ARROW_CIRCLE_DOWN)
    }
}
