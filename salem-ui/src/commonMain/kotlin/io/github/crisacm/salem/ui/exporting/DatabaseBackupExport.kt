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
package io.github.crisacm.salem.ui.exporting

import io.github.crisacm.salem.core.io.backupDatabase
import io.github.crisacm.salem.core.io.backupFileName
import io.github.crisacm.salem.platform.io.UserFile
import io.github.crisacm.salem.platform.time.LocalDate
import kotlinx.coroutines.CancellationException

/**
 * Exports the whole database: copies [source], the file of the database of the app, into the [folder] for exports and
 * hands the copy to [share] (the platform's share sheet). Anything that fails, the copy or the sharing, is reported
 * once through [onFailed]. The platform empties the folder before an export, so old copies do not pile up.
 */
class DatabaseBackupExport(
    private val source: UserFile,
    private val folder: suspend () -> UserFile,
    private val today: () -> LocalDate,
    private val share: (String) -> Unit,
    private val onFailed: () -> Unit
) {
    suspend fun start() {
        try {
            val target = folder().resolve(backupFileName(today()))
            backupDatabase(source, target)
            share(target.pathString)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            onFailed()
        }
    }
}
