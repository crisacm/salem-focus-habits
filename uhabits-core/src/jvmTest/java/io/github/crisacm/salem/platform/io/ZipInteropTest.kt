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

import java.io.File
import java.util.zip.ZipInputStream
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `ios-written.zip` is produced by the iOS ZipWriter (see AppleZipInteropTest, which pins its
 * bytes). Reading it here proves that the JDK accepts what iOS writes, including UTF-8 names.
 */
class ZipInteropTest {
    @Test
    fun testJdkReadsTheArchiveWrittenOnIos() {
        val zip = File("assets/test/zip/ios-written.zip")
        val entries = ZipInputStream(zip.inputStream()).use { zis ->
            generateSequence { zis.nextEntry }.map { it.name to zis.readBytes().decodeToString() }.toList()
        }
        assertEquals(
            listOf(
                "hello.txt" to "Hello, World!",
                "dir/ñandú.csv" to "año,día\n1,2\n",
                "empty.txt" to "",
                "big.txt" to "abc\n".repeat(20_000)
            ),
            entries
        )
    }
}
