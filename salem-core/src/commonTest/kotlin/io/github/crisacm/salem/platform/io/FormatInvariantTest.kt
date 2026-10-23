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

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatInvariantTest {
    @Test
    fun testFormatInvariant() {
        assertEquals("005", formatInvariant("%03d", 5))
        assertEquals("09.sql", formatInvariant("%02d.sql", 9))
        assertEquals("0.5000", formatInvariant("%.4f", 0.5))
        assertEquals("2.0", formatInvariant("%.1f", 2.0))
        assertEquals("00013.42", formatInvariant("%08.2f", 13.419187263))
    }
}
