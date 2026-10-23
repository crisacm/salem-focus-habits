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
import kotlin.test.assertFailsWith

class PrintfTest {
    @Test
    fun testIntegers() {
        assertEquals("  5", formatPrintf("%3d", 5))
        assertEquals("005", formatPrintf("%03d", 5))
        assertEquals("145", formatPrintf("%3d", 145))
        assertEquals("1234", formatPrintf("%3d", 1234))
        assertEquals("5  |", formatPrintf("%-3d|", 5))
        assertEquals("-05", formatPrintf("%03d", -5))
        assertEquals(" -5", formatPrintf("%3d", -5))
        assertEquals("09.sql", formatPrintf("%02d.sql", 9))
        assertEquals("widget-000042-habit", formatPrintf("widget-%06d-habit", 42))
        assertEquals("-2147483648", formatPrintf("%d", Int.MIN_VALUE))
        assertEquals("9223372036854775807", formatPrintf("%d", Long.MAX_VALUE))
    }

    @Test
    fun testStrings() {
        assertEquals("hello world!", formatPrintf("hello %s!", "world"))
        assertEquals("   ab", formatPrintf("%5s", "ab"))
        assertEquals("ab   |", formatPrintf("%-5s|", "ab"))
        assertEquals("ab", formatPrintf("%.2s", "abcdef"))
    }

    @Test
    fun testDoubles() {
        assertEquals("0.5000", formatPrintf("%.4f", 0.5))
        assertEquals("2.0", formatPrintf("%.1f", 2.0))
        assertEquals("13.42", formatPrintf("%.2f", 13.419187263))
        assertEquals("   13.42", formatPrintf("%8.2f", 13.419187263))
        assertEquals("00013.42", formatPrintf("%08.2f", 13.419187263))
        assertEquals("13.42   ", formatPrintf("%-8.2f", 13.419187263))
        assertEquals("-00013.42", formatPrintf("%09.2f", -13.419187263))
        assertEquals("0.500000", formatPrintf("%f", 0.5))
        assertEquals("100%", formatPrintf("%.0f%%", 100.0))
        assertEquals("1.5k", formatPrintf("%.1fk", 1.5))
        assertEquals("1234567890", formatPrintf("%.0f", 1234567890.0))
    }

    @Test
    fun testRoundsHalfUpLikeJava() {
        assertEquals("1", formatPrintf("%.0f", 0.5))
        assertEquals("2", formatPrintf("%.0f", 1.5))
        assertEquals("3", formatPrintf("%.0f", 2.5))
        assertEquals("0.13", formatPrintf("%.2f", 0.125))
        assertEquals("2.68", formatPrintf("%.2f", 2.675))
        assertEquals("1.0", formatPrintf("%.1f", 0.95))
        assertEquals("10", formatPrintf("%.0f", 9.5))
        assertEquals("1.00", formatPrintf("%.2f", 0.999))
        assertEquals("0.0000", formatPrintf("%.4f", 0.00001))
        assertEquals("0.0001", formatPrintf("%.4f", 0.00005))
    }

    @Test
    fun testSignsAndSpecialValues() {
        assertEquals("-0.0", formatPrintf("%.1f", -0.0))
        assertEquals("-0.0", formatPrintf("%.1f", -0.04))
        assertEquals("-1.50", formatPrintf("%.2f", -1.5))
        assertEquals("NaN", formatPrintf("%.1f", Double.NaN))
        assertEquals("Infinity", formatPrintf("%.1f", Double.POSITIVE_INFINITY))
        assertEquals("-Infinity", formatPrintf("%.1f", Double.NEGATIVE_INFINITY))
        assertEquals("  NaN", formatPrintf("%05.1f", Double.NaN))
    }

    @Test
    fun testExtremeMagnitudes() {
        assertEquals("1000000000000000000000.0", formatPrintf("%.1f", 1e21))
        assertEquals("0.0000001", formatPrintf("%.7f", 1e-7))
        assertEquals("0.000", formatPrintf("%.3f", 1e-7))
        assertEquals("12345678.900", formatPrintf("%.3f", 1.23456789E7))
    }

    @Test
    fun testDecimalSeparator() {
        assertEquals("13,42", formatPrintf("%.2f", 13.419187263, ','))
        assertEquals("0,500000", formatPrintf("%f", 0.5, ','))
        assertEquals("007", formatPrintf("%03d", 7, ','))
        assertEquals("a.b", formatPrintf("%s", "a.b", ','))
    }

    @Test
    fun testRejectsWhatItDoesNotSupport() {
        assertFailsWith<IllegalArgumentException> { formatPrintf("%x", 5) }
        assertFailsWith<IllegalArgumentException> { formatPrintf("%d", 1.5) }
        assertFailsWith<IllegalArgumentException> { formatPrintf("%f", 1) }
        assertFailsWith<IllegalArgumentException> { formatPrintf("%d %d", 1) }
        assertFailsWith<IllegalArgumentException> { formatPrintf("%-05d", 1) }
        assertFailsWith<IllegalArgumentException> { formatPrintf("%0d", 1) }
        assertFailsWith<IllegalArgumentException> { formatPrintf("100%", 1) }
    }
}
