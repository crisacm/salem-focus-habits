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

package org.isoron.platform.io

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * formatPrintf supports a subset of printf and fails at run time on anything else, so this lists
 * every format string used with [format] and [formatInvariant] in production code. Add a new
 * string here when a call site introduces one.
 */
class PrintfProductionFormatsTest {
    @Test
    fun testFormatStringsUsedByTheApp() {
        // NumberButton.kt
        assertEquals("1.5G", formatPrintf("%.1fG", 1.5))
        assertEquals("2M", formatPrintf("%.0fM", 2.0))
        assertEquals("2.5M", formatPrintf("%.1fM", 2.5))
        assertEquals("3k", formatPrintf("%.0fk", 3.0))
        assertEquals("3.5k", formatPrintf("%.1fk", 3.5))
        assertEquals("7", formatPrintf("%.0f", 7.0))
        assertEquals("7.5", formatPrintf("%.1f", 7.5))
        assertEquals("7.25", formatPrintf("%.2f", 7.25))
        // Ring.kt
        assertEquals("75%", formatPrintf("%.0f%%", 75.0))
        // WidgetPreferences.kt
        assertEquals("widget-000042-habit", formatPrintf("widget-%06d-habit", 42))
        assertEquals("snooze-000042", formatPrintf("snooze-%06d", 42))
        // HabitsCSVExporter.kt, HabitList.kt
        assertEquals("007", formatPrintf("%03d", 7))
        assertEquals("0.5000", formatPrintf("%.4f", 0.5))
        // LoopDBImporter.kt
        assertEquals("09.sql", formatPrintf("%02d.sql", 9))
        // HabitList.kt
        assertEquals("2.0", formatPrintf("%.1f", 2.0))
    }
}
