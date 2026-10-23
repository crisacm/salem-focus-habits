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

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppleZipReaderTest {
    private fun fixture(name: String) = readFileBytes("${assetsDir()}/test/zip/$name")

    private suspend fun entries(bytes: ByteArray) = ZipReader(bytes).entries().map { it.name to it.content }

    private suspend fun sample(): ByteArray {
        val writer = ZipWriter(0, 0x21)
        writer.addEntry("a.txt", "alpha alpha alpha alpha")
        writer.addEntry("dir/b.csv", "año,día\n1,2\n")
        return writer.toBytes()
    }

    @Test
    fun testReadsAnArchiveWrittenByTheJdk() = runTest {
        // The JDK flags its entries with a data descriptor, so the sizes only live in the central directory.
        val expected = listOf(
            "hello.txt" to "Hello, World!",
            "dir/" to "",
            "dir/ñandú.csv" to "año,día\n1,2\n",
            "empty.txt" to "",
            "big.txt" to "abc\n".repeat(20_000)
        )
        assertEquals(expected, entries(fixture("jdk-written.zip")))
    }

    @Test
    fun testReadsStoredEntries() = runTest {
        val expected = listOf("stored.txt" to "stored content\n", "más/ñ.txt" to "ünï\n")
        assertEquals(expected, entries(fixture("stored.zip")))
    }

    @Test
    fun testReadsWhatTheWriterWrites() = runTest {
        assertEquals(
            listOf("a.txt" to "alpha alpha alpha alpha", "dir/b.csv" to "año,día\n1,2\n"),
            entries(sample())
        )
        assertEquals(emptyList(), entries(ZipWriter().toBytes()))
    }

    @Test
    fun testRejectsInputThatIsNotAZip() = runTest {
        assertFailsWith<IllegalArgumentException> { entries(ByteArray(0)) }
        assertFailsWith<IllegalArgumentException> { entries("not a zip file at all, just text".encodeToByteArray()) }
        assertFailsWith<IllegalArgumentException> { entries(ByteArray(100)) }
    }

    @Test
    fun testRejectsTruncatedArchives() = runTest {
        val zip = sample()
        assertFailsWith<IllegalArgumentException> { entries(zip.copyOf(zip.size - 1)) }
        assertFailsWith<IllegalArgumentException> { entries(zip.copyOf(zip.size / 2)) }
    }

    @Test
    fun testRejectsACorruptEntry() = runTest {
        val zip = sample()
        // Flip a bit in the stored CRC of the first entry (local header and central directory must both be checked)
        val wrongCrc = zip.copyOf()
        val directory = zip.indexOfSignature(0x02014b50)
        wrongCrc[directory + 16] = (wrongCrc[directory + 16].toInt() xor 1).toByte()
        assertFailsWith<IllegalArgumentException> { entries(wrongCrc) }
        // Damage the compressed bytes
        val damaged = zip.copyOf()
        damaged[30 + "a.txt".length + 2] = (damaged[30 + "a.txt".length + 2].toInt() xor 0x55).toByte()
        assertFailsWith<IllegalArgumentException> { entries(damaged) }
    }

    @Test
    fun testRejectsUnsupportedFeatures() = runTest {
        val zip = sample()
        val directory = zip.indexOfSignature(0x02014b50)
        val method = zip.copyOf().also { it[directory + 10] = 12 }
        assertFailsWith<IllegalArgumentException> { entries(method) }
        val encrypted = zip.copyOf().also { it[directory + 8] = (it[directory + 8].toInt() or 1).toByte() }
        assertFailsWith<IllegalArgumentException> { entries(encrypted) }
        val badOffset = zip.copyOf().also {
            it[directory + 42] = 0x7F
            it[directory + 43] = 0x7F
        }
        assertFailsWith<IllegalArgumentException> { entries(badOffset) }
    }

    private fun ByteArray.indexOfSignature(signature: Int): Int {
        val bytes = byteArrayOf(signature.toByte(), (signature shr 8).toByte(), (signature shr 16).toByte(), (signature shr 24).toByte())
        for (i in 0..size - 4) {
            if (this[i] == bytes[0] && this[i + 1] == bytes[1] && this[i + 2] == bytes[2] && this[i + 3] == bytes[3]) return i
        }
        error("Signature not found")
    }
}
