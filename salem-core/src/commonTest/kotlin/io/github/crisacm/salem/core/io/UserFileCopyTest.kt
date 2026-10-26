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

package io.github.crisacm.salem.core.io

import io.github.crisacm.salem.core.BaseUnitTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserFileCopyTest : BaseUnitTest() {
    @Test
    fun copiesTheContentAndKeepsTheOriginal() = runTest {
        val dir = createTempDir()
        val source = dir.resolve("source.bin")
        val bytes = ByteArray(20_000) { (it % 251).toByte() }
        source.writeBytes(bytes)
        val target = dir.resolve("target.bin")

        source.copyTo(target)

        assertTrue(source.exists())
        assertContentEquals(bytes, target.readBytes(100_000))
    }

    @Test
    fun replacesAFileThatExists() = runTest {
        val dir = createTempDir()
        val source = dir.resolve("source.txt")
        source.writeString("new")
        val target = dir.resolve("target.txt")
        target.writeString("old and longer")

        source.copyTo(target)

        assertEquals(listOf("new"), target.lines())
    }

    @Test
    fun createsTheFolderOfTheTarget() = runTest {
        val dir = createTempDir()
        val source = dir.resolve("source.txt")
        source.writeString("content")
        val target = dir.resolve("a/b/target.txt")

        source.copyTo(target)

        assertEquals(listOf("content"), target.lines())
    }
}
