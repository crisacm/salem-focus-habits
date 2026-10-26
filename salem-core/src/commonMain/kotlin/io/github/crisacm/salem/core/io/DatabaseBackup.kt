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
import io.github.crisacm.salem.platform.time.LocalDate

/**
 * Copies the file of the database of the app to [target], replacing it. The copy keeps the schema version, so an
 * importer can migrate it later, and it holds everything that was saved: the apps open their database with a single
 * file (no write-ahead log), so the main file is complete between two writes. Call it when no import or command is
 * running.
 */
suspend fun backupDatabase(source: UserFile, target: UserFile) = source.copyTo(target)

/** The name of the backup file of a day. */
fun backupFileName(date: LocalDate): String = "Salem Backup ${date.toCSVString()}.db"
