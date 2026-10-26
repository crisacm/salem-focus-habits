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
import io.github.crisacm.salem.platform.io.UserFile
import io.github.crisacm.salem.platform.io.begin
import io.github.crisacm.salem.platform.io.commit
import io.github.crisacm.salem.platform.io.run
import kotlinx.coroutines.CancellationException

enum class ImportResult { SUCCESS, NOT_RECOGNIZED, FAILED }

/**
 * Imports one file inside a transaction of the database of the app: if anything fails, what was already imported is
 * rolled back, so the habits stay as they were. Then [onRolledBack] runs, so the caller can drop what it holds in
 * memory (the list of habits reads them again from the database).
 *
 * The importer may be given a file only once it is a copy: some importers migrate the file in place.
 */
class ImportRunner(
    private val importer: AbstractImporter,
    private val database: Database,
    private val onRolledBack: () -> Unit
) {
    suspend fun run(file: UserFile): ImportResult {
        database.begin()
        return try {
            if (importer.canHandle(file)) {
                importer.importHabitsFromFile(file)
                database.commit()
                ImportResult.SUCCESS
            } else {
                database.commit()
                ImportResult.NOT_RECOGNIZED
            }
        } catch (e: CancellationException) {
            rollBack()
            throw e
        } catch (e: Exception) {
            rollBack()
            ImportResult.FAILED
        }
    }

    private fun rollBack() {
        try {
            database.run("ROLLBACK")
        } catch (e: Exception) {
            // No transaction left to undo (the failure may have ended it); the habits are read again anyway
        } finally {
            onRolledBack()
        }
    }
}
