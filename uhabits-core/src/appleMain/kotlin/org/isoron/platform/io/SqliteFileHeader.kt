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

@file:OptIn(ExperimentalForeignApi::class)

package org.isoron.platform.io

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.O_RDONLY
import platform.posix.close
import platform.posix.open
import platform.posix.pread

private const val HEADER_SIZE = 100
private const val USER_VERSION_OFFSET = 60
private val SQLITE_MAGIC = "SQLite format 3".encodeToByteArray()

/**
 * Reads the `user_version` field of the header of a SQLite file, without opening the database.
 *
 * sqliter rewrites `user_version` when it is opened with a different version, so the opener needs to
 * know the current value first. Returns null if the file does not exist or is not a SQLite database
 * (missing, empty, shorter than a header or without the SQLite magic string).
 */
internal fun peekUserVersion(path: String): Int? {
    val fd = open(path, O_RDONLY)
    if (fd < 0) return null
    try {
        val header = ByteArray(HEADER_SIZE)
        val read = header.usePinned { pread(fd, it.addressOf(0), HEADER_SIZE.toULong(), 0) }
        if (read < HEADER_SIZE) return null
        for (i in SQLITE_MAGIC.indices) if (header[i] != SQLITE_MAGIC[i]) return null
        var version = 0
        for (i in 0 until 4) version = (version shl 8) or (header[USER_VERSION_OFFSET + i].toInt() and 0xFF)
        return version
    } finally {
        close(fd)
    }
}
