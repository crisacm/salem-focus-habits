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

package io.github.crisacm.salem.core.database

import io.github.crisacm.salem.core.DATABASE_VERSION
import io.github.crisacm.salem.platform.io.Database
import io.github.crisacm.salem.platform.io.getVersion
import io.github.crisacm.salem.platform.io.migrateTo
import io.github.crisacm.salem.platform.io.setVersion

/** The oldest schema that the migrations can start from; a new file starts here and is migrated up. */
const val OLDEST_SUPPORTED_DATABASE_VERSION = 8

/**
 * Prepares the habits database to be used: a new file (version 0) gets the oldest supported schema version and
 * is then migrated, an older one is migrated, and a file from a version that is too old, or newer than this app
 * knows, is refused with [UnsupportedDatabaseVersionException]. These are the rules of the Android opener, for
 * platforms that open the file themselves. Each migration is one transaction (Android's opener gets that from
 * `SQLiteOpenHelper`), so a crash in the middle leaves the file at the version before and it can be migrated again.
 */
suspend fun Database.initializeHabitsSchema(
    targetVersion: Int = DATABASE_VERSION,
    loadMigrationSQL: suspend (Int) -> String
) {
    if (getVersion() == 0) setVersion(OLDEST_SUPPORTED_DATABASE_VERSION)
    val version = getVersion()
    if (version < OLDEST_SUPPORTED_DATABASE_VERSION || version > targetVersion) throw UnsupportedDatabaseVersionException()
    migrateTo(targetVersion, atomic = true, loadMigrationSQL = loadMigrationSQL)
}
