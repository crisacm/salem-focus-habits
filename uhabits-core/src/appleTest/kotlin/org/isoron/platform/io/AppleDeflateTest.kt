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

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AppleDeflateTest {
    @Test
    fun testCrc32KnownVectors() {
        assertEquals(0, crc32Of(ByteArray(0)))
        assertEquals(0xCBF43926.toInt(), crc32Of("123456789".encodeToByteArray()))
        assertEquals(0x414FA339, crc32Of("The quick brown fox jumps over the lazy dog".encodeToByteArray()))
    }

    @Test
    fun testDeflateRoundTrips() {
        val inputs = listOf(
            ByteArray(0),
            "a".encodeToByteArray(),
            "Hábito ñandú 日本語 😀".encodeToByteArray(),
            Random(1).nextBytes(1_000_000),
            ByteArray(50_000) { (it % 7).toByte() }
        )
        for (input in inputs) {
            assertContentEquals(input, inflateRaw(deflateRaw(input), input.size), "size=${input.size}")
        }
    }

    @Test
    fun testDeflateCompressesRepetitiveData() {
        val text = "x".repeat(100_000).encodeToByteArray()
        assertTrue(deflateRaw(text).size < 1_000)
    }

    @Test
    fun testInflateRejectsCorruptData() {
        val deflated = deflateRaw("hello hello hello hello".encodeToByteArray())
        assertFailsWith<IllegalArgumentException> { inflateRaw(byteArrayOf(0x07, 0x07, 0x07, 0x07), 100) }
        assertFailsWith<IllegalArgumentException> { inflateRaw(deflated.copyOf(deflated.size / 2), 23) }
    }

    @Test
    fun testInflateChecksTheExpectedSize() {
        val input = "hello hello hello hello".encodeToByteArray()
        val deflated = deflateRaw(input)
        assertFailsWith<IllegalArgumentException> { inflateRaw(deflated, input.size - 1) }
        assertFailsWith<IllegalArgumentException> { inflateRaw(deflated, input.size + 1) }
    }

    @Test
    fun testDosDateTime() {
        val start = dosDateTime(2000, 1, 1, 0, 0, 0)
        assertEquals(0, start.time)
        assertEquals((20 shl 9) or (1 shl 5) or 1, start.date)
        val late = dosDateTime(2024, 12, 31, 23, 59, 58)
        assertEquals((23 shl 11) or (59 shl 5) or 29, late.time)
        assertEquals((44 shl 9) or (12 shl 5) or 31, late.date)
        // Seconds have a two second resolution and years start in 1980.
        assertEquals(dosDateTime(2001, 2, 3, 4, 5, 6).time, dosDateTime(2001, 2, 3, 4, 5, 7).time)
        assertEquals(0, dosDateTime(1970, 5, 5, 0, 0, 0).date shr 9)
    }
}
