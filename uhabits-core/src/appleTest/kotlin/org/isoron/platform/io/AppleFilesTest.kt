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
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppleFilesTest {
    private val root = tempPath("salem-apple-files-test")

    private fun file(name: String) = AppleUserFile("$root/$name")

    @AfterTest
    fun tearDown() {
        deleteIfExists(root)
    }

    @Test
    fun testWriteAndReadBytes() = runTest {
        val f = file("bytes.bin")
        for (size in listOf(0, 1, 7, 1024 * 1024)) {
            val bytes = ByteArray(size) { (it * 31).toByte() }
            f.writeBytes(bytes)
            assertContentEquals(bytes, f.readBytes(size + 10), "size=$size")
        }
    }

    @Test
    fun testWriteReplacesTheContent() = runTest {
        val f = file("replace.txt")
        f.writeString("a long first content")
        f.writeString("short")
        assertEquals(listOf("short"), f.lines())
        assertContentEquals("short".encodeToByteArray(), f.readBytes(100))
    }

    @Test
    fun testWriteCreatesTheParentDirectories() = runTest {
        val f = file("a/b/c/deep.txt")
        f.writeString("x")
        assertTrue(f.exists())
    }

    @Test
    fun testLinesSplitsLikeReadAllLines() = runTest {
        val f = file("lines.txt")
        f.writeString("a\nb\n")
        assertEquals(listOf("a", "b"), f.lines())
        f.writeString("a\r\nb\r\n\r\nc")
        assertEquals(listOf("a", "b", "", "c"), f.lines())
        f.writeString("a\rb")
        assertEquals(listOf("a", "b"), f.lines())
        f.writeString("")
        assertEquals(emptyList(), f.lines())
        f.writeString("\n")
        assertEquals(listOf(""), f.lines())
    }

    @Test
    fun testLinesDecodesUtf8() = runTest {
        val f = file("utf8.txt")
        f.writeString("Hábito ñandú\n日本語 😀\n")
        assertEquals(listOf("Hábito ñandú", "日本語 😀"), f.lines())
    }

    @Test
    fun testLinesOfAMissingFileThrows() = runTest {
        assertFailsWith<Exception> { file("missing.txt").lines() }
    }

    @Test
    fun testExistsAndDelete() = runTest {
        val f = file("exists.txt")
        assertFalse(f.exists())
        f.writeString("x")
        assertTrue(f.exists())
        f.delete()
        assertFalse(f.exists())
    }

    @Test
    fun testDeleteOfAMissingFileDoesNothing() = runTest {
        file("never-created.txt").delete()
    }

    @Test
    fun testReadBytesHonorsTheLimit() = runTest {
        val f = file("limit.bin")
        val bytes = ByteArray(10) { it.toByte() }
        f.writeBytes(bytes)
        assertContentEquals(bytes.copyOf(3), f.readBytes(3))
        assertContentEquals(bytes, f.readBytes(10))
        assertContentEquals(bytes, f.readBytes(1000))
        assertContentEquals(ByteArray(0), f.readBytes(0))
    }

    @Test
    fun testReadBytesOfAMissingFileThrows() = runTest {
        assertFailsWith<Exception> { file("missing.bin").readBytes(4) }
    }

    @Test
    fun testPathString() {
        assertEquals("$root/p.txt", file("p.txt").pathString)
    }

    @Test
    fun testMkdirsCreatesNestedDirectoriesAndIsIdempotent() = runTest {
        val dir = file("x/y/z")
        assertFalse(dir.exists())
        dir.mkdirs()
        assertTrue(dir.exists())
        dir.mkdirs()
        assertTrue(dir.exists())
    }

    @Test
    fun testResolveIsRelativeToThePathItself() = runTest {
        val dir = file("dir")
        dir.mkdirs()
        val child = dir.resolve("child.txt")
        assertEquals("$root/dir/child.txt", child.pathString)
        child.writeString("hi")
        assertEquals(listOf("hi"), child.lines())
    }

    @Test
    fun testListFilesReturnsSortedChildren() = runTest {
        val dir = file("list")
        dir.mkdirs()
        dir.resolve("b.txt").writeString("b")
        dir.resolve("a.txt").writeString("a")
        dir.resolve("sub").mkdirs()
        val children = dir.listFiles()
        assertEquals(
            listOf("$root/list/a.txt", "$root/list/b.txt", "$root/list/sub"),
            children?.map { it.pathString }
        )
    }

    @Test
    fun testListFilesOfAnEmptyDirectoryIsEmpty() = runTest {
        val dir = file("empty")
        dir.mkdirs()
        assertEquals(emptyList(), dir.listFiles()?.map { it.pathString })
    }

    @Test
    fun testListFilesIsNullForMissingPathsAndRegularFiles() = runTest {
        assertNull(file("nothing-here").listFiles())
        val f = file("regular.txt")
        f.writeString("x")
        assertNull(f.listFiles())
    }

    @Test
    fun testDeleteRemovesADirectoryWithItsContent() = runTest {
        val dir = file("tree")
        dir.resolve("a/b.txt").writeString("x")
        dir.delete()
        assertFalse(dir.exists())
    }
}
