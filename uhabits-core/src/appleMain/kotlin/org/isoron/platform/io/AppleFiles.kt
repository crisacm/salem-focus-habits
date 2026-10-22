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

@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package org.isoron.platform.io

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import org.isoron.platform.gui.Image
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.posix.O_RDONLY
import platform.posix.close
import platform.posix.open
import platform.posix.read

/**
 * Opens the files of the app. Like AndroidFileOpener, it receives its roots instead of looking them up.
 *
 * User files live under [userDir]. A resource is looked up in [resourceRoots] in order, so tests can
 * list the main assets before the test assets, as JavaFileOpener does. A resource that exists in no
 * root is opened on the last one, so [ResourceFile.exists] is false instead of failing.
 */
class AppleFileOpener(
    private val userDir: String,
    private val resourceRoots: List<String>
) : FileOpener {
    override fun openResourceFile(path: String): ResourceFile {
        val fm = NSFileManager.defaultManager
        val root = resourceRoots.firstOrNull { fm.fileExistsAtPath("$it/$path") } ?: resourceRoots.last()
        return AppleResourceFile("$root/$path")
    }

    override fun openUserFile(path: String): UserFile = AppleUserFile("$userDir/$path")

    companion object {
        /**
         * User files in Application Support (backed up, not shown in the Files app) and resources in
         * the "assets" folder of the app bundle. The app target must copy `uhabits-core/assets/main`
         * there (migration step 7).
         */
        fun createDefault(): AppleFileOpener {
            val fm = NSFileManager.defaultManager
            val url = fm.URLsForDirectory(NSApplicationSupportDirectory, NSUserDomainMask).first() as NSURL
            val userDir = checkNotNull(url.path) { "No Application Support directory" }
            check(fm.createDirectoryAtPath(userDir, withIntermediateDirectories = true, attributes = null, error = null)) {
                "Cannot create $userDir"
            }
            val bundle = NSBundle.mainBundle.resourcePath ?: ""
            return AppleFileOpener(userDir, listOf("$bundle/assets"))
        }
    }
}

/**
 * A file shipped with the app. [path] is absolute.
 */
class AppleResourceFile(private val path: String) : ResourceFile {
    override suspend fun copyTo(dest: UserFile) {
        require(dest is AppleUserFile) { "Expected an AppleUserFile" }
        dest.delete()
        createParentDirectories(dest.pathString)
        check(NSFileManager.defaultManager.copyItemAtPath(path, dest.pathString, null)) {
            "Cannot copy $path to ${dest.pathString}"
        }
    }

    override suspend fun lines(): List<String> = decodeLines(readFileBytes(path))

    override suspend fun exists(): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)

    override suspend fun toImage(): Image =
        throw UnsupportedOperationException("Images are not available on iOS until migration steps 6-7")
}

/**
 * A file in the user area of the app (databases, backups, exports). [path] must be absolute,
 * because importers hand it to the SQLite opener.
 *
 * Unlike JavaUserFile, [delete] does nothing when the file is missing, as the [UserFile]
 * contract says. [resolve] is relative to this path itself, like on the JVM.
 */
class AppleUserFile(private val path: String) : UserFile {
    override val pathString: String
        get() = path

    override suspend fun delete() {
        if (!exists()) return
        check(NSFileManager.defaultManager.removeItemAtPath(path, null)) { "Cannot delete $path" }
    }

    override suspend fun exists(): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)

    override suspend fun lines(): List<String> = decodeLines(readFileBytes(path))

    override suspend fun writeString(content: String) = writeBytes(content.encodeToByteArray())

    override suspend fun writeBytes(bytes: ByteArray) {
        createParentDirectories(path)
        check(bytes.toNSData().writeToFile(path, atomically = true)) { "Cannot write $path" }
    }

    override suspend fun readBytes(limit: Int): ByteArray = readFileBytes(path, limit)

    override fun resolve(child: String): UserFile = AppleUserFile("$path/$child")

    /**
     * The children sorted by name, or null if this is not a directory.
     */
    override suspend fun listFiles(): List<UserFile>? {
        val names = NSFileManager.defaultManager.contentsOfDirectoryAtPath(path, null) ?: return null
        return names.filterIsInstance<String>().sorted().map { AppleUserFile("$path/$it") }
    }

    override suspend fun mkdirs() {
        check(
            NSFileManager.defaultManager.createDirectoryAtPath(
                path,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        ) { "Cannot create $path" }
    }
}

internal fun createParentDirectories(path: String) {
    val parent = path.substringBeforeLast('/', "")
    if (parent.isEmpty()) return
    check(
        NSFileManager.defaultManager.createDirectoryAtPath(
            parent,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    ) { "Cannot create $parent" }
}

internal fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { NSData.create(bytes = it.addressOf(0), length = size.convert()) }
}

/**
 * Reads up to [limit] bytes of the file, looping until the limit or the end of the file.
 * Throws if the file cannot be opened.
 */
internal fun readFileBytes(path: String, limit: Int = Int.MAX_VALUE): ByteArray {
    val fd = open(path, O_RDONLY)
    check(fd >= 0) { "Cannot open $path" }
    try {
        var data = ByteArray(minOf(limit, 8192))
        var size = 0
        while (size < limit) {
            if (size == data.size) {
                data = data.copyOf(minOf(limit.toLong(), data.size * 2L).toInt())
            }
            val count = data.usePinned { read(fd, it.addressOf(size), (data.size - size).convert()) }
            check(count >= 0) { "Cannot read $path" }
            if (count == 0L) break
            size += count.toInt()
        }
        return data.copyOf(size)
    } finally {
        close(fd)
    }
}

/**
 * Decodes UTF-8 and splits on LF, CRLF and CR without a trailing empty line, like
 * java.nio.file.Files.readAllLines.
 */
internal fun decodeLines(bytes: ByteArray): List<String> {
    val text = bytes.decodeToString()
    val lines = ArrayList<String>()
    val line = StringBuilder()
    var i = 0
    while (i < text.length) {
        val c = text[i++]
        if (c == '\n' || c == '\r') {
            if (c == '\r' && i < text.length && text[i] == '\n') i++
            lines.add(line.toString())
            line.clear()
        } else {
            line.append(c)
        }
    }
    if (line.isNotEmpty()) lines.add(line.toString())
    return lines
}
