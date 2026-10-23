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

package io.github.crisacm.salem.platform.io

import platform.Foundation.NSLocale
import kotlin.test.Test
import kotlin.test.assertEquals

class AppleStringsTest {
    @Test
    fun testDecimalSeparatorFollowsTheLocale() {
        assertEquals('.', localeDecimalSeparator(NSLocale("en_US")))
        assertEquals(',', localeDecimalSeparator(NSLocale("es_CO")))
        assertEquals(',', localeDecimalSeparator(NSLocale("de_DE")))
    }

    @Test
    fun testFormatInvariantIgnoresTheLocale() {
        // Whatever the device locale, files and CSV keep ASCII digits and '.'.
        assertEquals("0.5000", formatInvariant("%.4f", 0.5))
        assertEquals("13.42", formatInvariant("%.2f", 13.419187263))
    }
}
