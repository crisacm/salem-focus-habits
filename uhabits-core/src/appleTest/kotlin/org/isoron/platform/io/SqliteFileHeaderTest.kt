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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SqliteFileHeaderTest {
    @Test
    fun testPeekReadsTheUserVersionOfRealDatabases() {
        assertEquals(12, peekUserVersion("${assetsDir()}/test/loop.db"))
        assertEquals(24, peekUserVersion("${assetsDir()}/test/rewire.db"))
        assertEquals(13, peekUserVersion("${assetsDir()}/test/tickmate.db"))
        assertEquals(21, peekUserVersion("${assetsDir()}/test/databases/021.db"))
        assertEquals(22, peekUserVersion("${assetsDir()}/test/databases/022.db"))
    }

    @Test
    fun testPeekReturnsNullForMissingEmptyOrShortFiles() {
        assertNull(peekUserVersion(tempPath("header_missing.db").also { deleteIfExists(it) }))
        val empty = tempPath("header_empty.db")
        createFile(empty, "")
        assertNull(peekUserVersion(empty))
        val short = tempPath("header_short.db")
        createFile(short, "0123456789")
        assertNull(peekUserVersion(short))
    }
}
