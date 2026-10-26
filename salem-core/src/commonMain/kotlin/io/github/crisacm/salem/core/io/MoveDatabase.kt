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

import io.github.crisacm.salem.platform.io.UserFile

private val SQLITE_HEADER = "SQLite format 3".encodeToByteArray()

/**
 * Moves the database of the app to its new place (the folder shared with the extensions). Does nothing, and returns
 * false, when there is no old database or the new place already has one (that one is the one in use). The old file is
 * deleted only after the copy is checked to be a SQLite database, so an interrupted move keeps the data.
 */
suspend fun moveDatabase(old: UserFile, new: UserFile): Boolean {
    if (!old.exists() || new.exists()) return false
    old.copyTo(new)
    if (!new.readBytes(SQLITE_HEADER.size).contentEquals(SQLITE_HEADER)) {
        new.delete()
        return false
    }
    old.delete()
    return true
}
