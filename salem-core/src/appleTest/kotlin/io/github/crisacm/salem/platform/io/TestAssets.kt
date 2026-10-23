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

package io.github.crisacm.salem.platform.io

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

/** The `salem-core/assets` directory of the repository. The simulator reads host paths. */
internal fun assetsDir(): String =
    NSProcessInfo.processInfo.environment["SALEM_ASSETS_DIR"] as? String
        ?: error("SALEM_ASSETS_DIR is not set; run the iOS tests through Gradle")

internal fun readAssetText(relative: String): String =
    NSString.stringWithContentsOfFile("${assetsDir()}/$relative", NSUTF8StringEncoding, null)
        ?: error("Cannot read asset $relative")

internal fun tempPath(name: String): String = NSTemporaryDirectory().trimEnd('/') + "/" + name

internal fun fileExists(path: String): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)

internal fun deleteIfExists(path: String) {
    NSFileManager.defaultManager.removeItemAtPath(path, null)
}

/** Copies a test asset to a fresh temporary file, so tests never modify the repository. */
internal fun copyAssetToTemp(relative: String, name: String): String {
    val destination = tempPath(name)
    deleteIfExists(destination)
    check(NSFileManager.defaultManager.copyItemAtPath("${assetsDir()}/$relative", destination, null)) {
        "Cannot copy asset $relative"
    }
    return destination
}

internal fun createFile(path: String, content: String) {
    deleteIfExists(path)
    val text = content as NSString
    check(text.writeToFile(path, atomically = false, encoding = NSUTF8StringEncoding, error = null)) {
        "Cannot write $path"
    }
}
