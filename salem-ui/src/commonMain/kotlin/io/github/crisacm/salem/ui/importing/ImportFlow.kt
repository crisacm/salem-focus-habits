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

package io.github.crisacm.salem.ui.importing

import io.github.crisacm.salem.core.io.ImportResult
import io.github.crisacm.salem.platform.io.UserFile
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.could_not_import
import io.github.crisacm.salem.ui.resources.file_not_recognized
import io.github.crisacm.salem.ui.resources.habits_imported
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.StringResource

/** The message that tells the user how an import ended. */
fun importMessage(result: ImportResult): StringResource = when (result) {
    ImportResult.SUCCESS -> Res.string.habits_imported
    ImportResult.NOT_RECOGNIZED -> Res.string.file_not_recognized
    ImportResult.FAILED -> Res.string.could_not_import
}

/**
 * What happens when the user chooses to import data: the platform picks a file ([pickFile] gives a copy that can be
 * changed, or null when the user cancels), the [runner] imports it and the copy is deleted whatever the result.
 * [refresh] runs when the habits of the screen may have changed (after an import, and after a failed one, whose
 * rollback made the app read them again). [onFinished] tells the result.
 */
class ImportFlow(
    private val pickFile: suspend () -> UserFile?,
    private val runner: suspend (UserFile) -> ImportResult,
    private val refresh: () -> Unit,
    private val onFinished: (ImportResult) -> Unit
) {
    suspend fun start() {
        val file = try {
            pickFile() ?: return
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            onFinished(ImportResult.FAILED)
            return
        }
        val result = try {
            runner(file)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ImportResult.FAILED
        } finally {
            deleteQuietly(file)
        }
        if (result != ImportResult.NOT_RECOGNIZED) refresh()
        onFinished(result)
    }

    // A copy that stays in the temporary folder is not worth losing the result for
    private suspend fun deleteQuietly(file: UserFile) {
        try {
            file.delete()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The system cleans the temporary folder
        }
    }
}
