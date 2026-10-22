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

class AppleZipInteropTest {
    /**
     * The JVM test ZipInteropTest reads the fixture with java.util.zip. Keeping the writer
     * byte-for-byte equal to it keeps that check meaningful: if the format changes on purpose,
     * regenerate assets/test/zip/ios-written.zip with this writer and the same entries.
     */
    @Test
    fun testWriterProducesTheCommittedFixture() = runTest {
        val stamp = dosDateTime(2020, 1, 2, 3, 4, 6)
        val writer = ZipWriter(stamp.time, stamp.date)
        writer.addEntry("hello.txt", "Hello, World!")
        writer.addEntry("dir/ñandú.csv", "año,día\n1,2\n")
        writer.addEntry("empty.txt", "")
        writer.addEntry("big.txt", "abc\n".repeat(20_000))
        assertContentEquals(readFileBytes("${assetsDir()}/test/zip/ios-written.zip"), writer.toBytes())
    }
}
