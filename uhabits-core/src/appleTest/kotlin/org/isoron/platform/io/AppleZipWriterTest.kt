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

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AppleZipWriterTest {
    private fun ByteArray.u16(at: Int) = (this[at].toInt() and 0xFF) or ((this[at + 1].toInt() and 0xFF) shl 8)

    private fun ByteArray.u32(at: Int) = u16(at).toLong() or (u16(at + 2).toLong() shl 16)

    private fun ByteArray.text(at: Int, length: Int) = copyOfRange(at, at + length).decodeToString()

    @Test
    fun testSingleEntryLayout() = runTest {
        val writer = ZipWriter(0x1234, 0x5678)
        writer.addEntry("hello.txt", "Hello, Hello, Hello, World!")
        val zip = writer.toBytes()
        val content = "Hello, Hello, Hello, World!".encodeToByteArray()

        // Local file header
        assertEquals(0x04034b50L, zip.u32(0))
        assertEquals(20, zip.u16(4))
        assertEquals(0x0800, zip.u16(6))
        assertEquals(8, zip.u16(8))
        assertEquals(0x1234, zip.u16(10))
        assertEquals(0x5678, zip.u16(12))
        assertEquals(crc32Of(content).toLong() and 0xFFFFFFFFL, zip.u32(14))
        assertEquals(content.size.toLong(), zip.u32(22))
        assertEquals(9, zip.u16(26))
        assertEquals(0, zip.u16(28))
        assertEquals("hello.txt", zip.text(30, 9))
        val compressed = zip.u32(18).toInt()
        assertContentEquals(content, inflateRaw(zip.copyOfRange(39, 39 + compressed), content.size))

        // End of central directory
        val end = zip.size - 22
        assertEquals(0x06054b50L, zip.u32(end))
        assertEquals(1, zip.u16(end + 8))
        assertEquals(1, zip.u16(end + 10))
        val directoryOffset = zip.u32(end + 16).toInt()
        assertEquals(39 + compressed, directoryOffset)
        assertEquals(zip.u32(end + 12).toInt(), end - directoryOffset)

        // Central directory header
        assertEquals(0x02014b50L, zip.u32(directoryOffset))
        assertEquals(0x0800, zip.u16(directoryOffset + 8))
        assertEquals(8, zip.u16(directoryOffset + 10))
        assertEquals(0x1234, zip.u16(directoryOffset + 12))
        assertEquals(0x5678, zip.u16(directoryOffset + 14))
        assertEquals(crc32Of(content).toLong() and 0xFFFFFFFFL, zip.u32(directoryOffset + 16))
        assertEquals(compressed.toLong(), zip.u32(directoryOffset + 20))
        assertEquals(content.size.toLong(), zip.u32(directoryOffset + 24))
        assertEquals(0L, zip.u32(directoryOffset + 42))
        assertEquals("hello.txt", zip.text(directoryOffset + 46, 9))
    }

    @Test
    fun testNamesAreUtf8() = runTest {
        val writer = ZipWriter()
        writer.addEntry("ñandú/日本.csv", "x")
        val zip = writer.toBytes()
        val nameBytes = "ñandú/日本.csv".encodeToByteArray()
        assertEquals(nameBytes.size, zip.u16(26))
        assertEquals("ñandú/日本.csv", zip.text(30, nameBytes.size))
        assertEquals(0x0800, zip.u16(6) and 0x0800)
    }

    @Test
    fun testEmptyEntryAndEmptyArchive() = runTest {
        val writer = ZipWriter()
        writer.addEntry("empty.txt", "")
        val zip = writer.toBytes()
        assertEquals(0L, zip.u32(14))
        assertEquals(0L, zip.u32(22))

        val none = ZipWriter().toBytes()
        assertEquals(22, none.size)
        assertEquals(0x06054b50L, none.u32(0))
        assertEquals(0, none.u16(10))
    }

    @Test
    fun testManyEntriesKeepTheirOrder() = runTest {
        val writer = ZipWriter()
        for (i in 0 until 300) writer.addEntry("dir/file-$i.csv", "row $i\n".repeat(i % 5 + 1))
        val zip = writer.toBytes()
        val end = zip.size - 22
        assertEquals(300, zip.u16(end + 10))
        var at = zip.u32(end + 16).toInt()
        for (i in 0 until 300) {
            assertEquals(0x02014b50L, zip.u32(at))
            val nameLength = zip.u16(at + 28)
            assertEquals("dir/file-$i.csv", zip.text(at + 46, nameLength))
            at += 46 + nameLength
        }
        assertEquals(end, at)
    }

    @Test
    fun testCompressesRepetitiveContent() = runTest {
        val writer = ZipWriter()
        writer.addEntry("big.txt", "x".repeat(100_000))
        assertTrue(writer.toBytes().size < 1_000)
    }

    @Test
    fun testRejectsDuplicatesAndLateEntries() = runTest {
        val writer = ZipWriter()
        writer.addEntry("a.txt", "a")
        assertFailsWith<IllegalArgumentException> { writer.addEntry("a.txt", "again") }
        val first = writer.toBytes()
        assertContentEquals(first, writer.toBytes())
        assertFailsWith<IllegalStateException> { writer.addEntry("b.txt", "b") }
    }

    @Test
    fun testRejectsEmptyAndTooLongNames() {
        val writer = ZipWriter()
        assertFailsWith<IllegalArgumentException> { writer.addEntry("", "x") }
        assertFailsWith<IllegalArgumentException> { writer.addEntry("a".repeat(70_000), "x") }
    }

    @Test
    fun testDefaultTimestampIsCurrent() = runTest {
        val writer = ZipWriter()
        writer.addEntry("a.txt", "a")
        val zip = writer.toBytes()
        val year = 1980 + (zip.u16(12) shr 9)
        assertTrue(year in 2020..2100, "year=$year")
    }
}
