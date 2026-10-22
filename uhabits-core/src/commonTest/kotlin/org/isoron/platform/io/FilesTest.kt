package org.isoron.platform.io

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FilesTest {
    private val fileOpener = createTestFileOpener()

    @Test
    fun testWriteStringAndLines() = runTest {
        val file = fileOpener.openUserFile("test-write-string")
        file.writeString("hello\nworld\n")
        val lines = file.lines()
        assertEquals(listOf("hello", "world"), lines)
        file.delete()
    }

    @Test
    fun testWriteBytesAndReadBytes() = runTest {
        val file = fileOpener.openUserFile("test-write-bytes")
        val data = byteArrayOf(0x53, 0x51, 0x4C, 0x69, 0x74, 0x65)
        file.writeBytes(data)
        val read = file.readBytes(6)
        assertTrue(data.contentEquals(read))
        file.delete()
    }

    @Test
    fun testReadBytesWithLimit() = runTest {
        val file = fileOpener.openUserFile("test-read-limit")
        file.writeBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8))
        val read = file.readBytes(3)
        assertEquals(3, read.size)
        assertEquals(1, read[0])
        assertEquals(2, read[1])
        assertEquals(3, read[2])
        file.delete()
    }

    @Test
    fun testExists() = runTest {
        val file = fileOpener.openUserFile("test-exists")
        file.writeString("data")
        assertTrue(file.exists())
        file.delete()
        assertFalse(file.exists())
    }

    @Test
    fun testMkdirsResolveAndListFiles() = runTest {
        assertNull(fileOpener.openUserFile("test-list-files-never-created").listFiles())
        val dir = fileOpener.openUserFile("test-list-files-dir")
        dir.mkdirs()
        assertTrue(dir.exists())
        assertEquals(0, dir.listFiles()?.size)
        dir.resolve("b.txt").writeString("b")
        dir.resolve("a.txt").writeString("a")
        val names = dir.listFiles()!!.map { it.pathString.substringAfterLast('/') }
        assertEquals(listOf("a.txt", "b.txt"), names.sorted())
        assertEquals(listOf("a"), dir.resolve("a.txt").lines())
        dir.resolve("a.txt").delete()
        dir.resolve("b.txt").delete()
        dir.delete()
    }

    @Test
    fun testLinesOfAMissingFileThrows() = runTest {
        assertFails { fileOpener.openUserFile("test-missing-lines-never-created").lines() }
    }

    @Test
    fun testResourceFileCopyTo() = runTest {
        val resource = fileOpener.openResourceFile("loop.db")
        assertTrue(resource.exists())
        val dest = fileOpener.openUserFile("test-copy-to/loop.db")
        resource.copyTo(dest)
        assertEquals("SQLite format 3", dest.readBytes(15).decodeToString())
        // Copying again replaces the file.
        resource.copyTo(dest)
        assertTrue(dest.exists())
        dest.delete()
    }

    @Test
    fun testMissingResourceFile() = runTest {
        val resource = fileOpener.openResourceFile("no-such-resource.txt")
        assertFalse(resource.exists())
        assertFails { resource.lines() }
    }
}
