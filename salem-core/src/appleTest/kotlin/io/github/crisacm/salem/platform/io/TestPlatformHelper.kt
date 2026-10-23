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

import io.github.crisacm.salem.platform.gui.Canvas
import io.github.crisacm.salem.platform.time.AppleLocalDateFormatter
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import platform.Foundation.NSLocale

// Stubs so the commonTest sources link on iOS. The real implementations come in migration steps 3 and 4.

actual fun createTestFileOpener(): FileOpener = AppleFileOpener(
    userDir = tempPath("salem-test-files"),
    resourceRoots = listOf("${assetsDir()}/main", "${assetsDir()}/test")
)

actual fun createTestDatabaseOpener(): DatabaseOpener = SqliterDatabaseOpener()

actual fun createTestCanvas(width: Int, height: Int): Canvas = TODO("iOS: step 4")

actual fun createTestDateFormatter(): LocalDateFormatter = AppleLocalDateFormatter(NSLocale("en_US"))
