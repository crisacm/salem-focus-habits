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

package io.github.crisacm.salem.ui.about

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** The links of the shared screens are the ones of `constants.xml`, which the Android intents and widgets use. */
class SalemLinksTest {
    private val constants = File("../salem-android/src/main/res/values/constants.xml").readText()

    private fun url(name: String): String {
        val match = Regex("<string name=\"$name\"[^>]*>([^<]*)</string>").find(constants)
        return checkNotNull(match) { "$name is not in constants.xml" }.groupValues[1]
    }

    @Test
    fun matchesConstantsXml() {
        assertEquals(url("helpURL"), SalemLinks.HELP)
        assertEquals(url("playStoreURL"), SalemLinks.RATE)
        assertEquals(url("feedbackURL"), SalemLinks.FEEDBACK)
        assertEquals(url("privacyPolicyURL"), SalemLinks.PRIVACY)
        assertEquals(url("codeContributorsURL"), SalemLinks.CONTRIBUTORS)
        assertEquals(url("sourceCodeURL"), SalemLinks.SOURCE)
        assertEquals(url("translateURL"), SalemLinks.TRANSLATE)
    }
}
