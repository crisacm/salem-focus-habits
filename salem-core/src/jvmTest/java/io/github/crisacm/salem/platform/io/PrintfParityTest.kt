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

import java.util.Locale
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Checks [formatPrintf] against java.util.Formatter, which the Android and JVM actuals use.
 */
class PrintfParityTest {
    private val integerFormats = listOf("%d", "%3d", "%03d", "%06d", "%-6d", "%02d.sql", "widget-%06d-habit")
    private val doubleFormats = listOf(
        "%f", "%.0f", "%.1f", "%.2f", "%.4f", "%8.2f", "%08.2f", "%-8.2f", "%.0f%%", "%.1fk"
    )

    @Test
    fun testIntegersMatchJava() {
        val random = Random(1)
        val values = listOf(0, 1, -1, 9, 10, 99, 100, 999, 123456, Int.MAX_VALUE, Int.MIN_VALUE) +
            List(500) { random.nextInt() } + List(500) { random.nextInt(-5000, 5000) }
        for (f in integerFormats) {
            for (v in values) {
                assertEquals(String.format(Locale.ROOT, f, v), formatPrintf(f, v), "format=$f value=$v")
            }
        }
    }

    @Test
    fun testDoublesMatchJava() {
        val random = Random(2)
        // Values with few decimals, where the shortest representation is unambiguous, and ties.
        val values = listOf(0.0, -0.0, 0.5, 1.5, 2.5, 0.125, 2.675, 0.95, 9.5, 0.999, 1e-5, 5e-5, 1e7, 123456.789) +
            List(1000) { random.nextInt(-2_000_000, 2_000_000) / 10_000.0 } +
            List(500) { random.nextInt(0, 1000) / 8.0 } +
            List(500) { random.nextInt(0, 100_000_000) / 1000.0 }
        for (f in doubleFormats) {
            for (v in values) {
                assertEquals(String.format(Locale.ROOT, f, v), formatPrintf(f, v), "format=$f value=$v")
            }
        }
    }
}
