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

import io.github.crisacm.salem.platform.io.Database
import io.github.crisacm.salem.platform.io.queryInt

/**
 * Tells whether another connection (an extension of the app, for example) committed changes to the database since the
 * last time it was asked, through SQLite's `data_version`, which the changes of this same connection do not move.
 */
class ExternalChanges(private val database: Database) {
    private var seen = dataVersion()

    fun hasChanged(): Boolean {
        val now = dataVersion()
        val changed = now != seen
        seen = now
        return changed
    }

    private fun dataVersion() = database.queryInt("PRAGMA data_version")
}
