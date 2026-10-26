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

import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.crisacm.salem.core.io.ImportResult
import io.github.crisacm.salem.platform.io.UserFile
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImportFlowTest {
    private val file = mock<UserFile>()
    private var picked: UserFile? = file
    private var pickFailure: Exception? = null
    private var runnerResult: ImportResult = ImportResult.SUCCESS
    private var runnerFailure: Exception? = null
    private var runs = 0
    private var refreshes = 0
    private val finished = mutableListOf<ImportResult>()

    private val flow = ImportFlow(
        pickFile = {
            pickFailure?.let { throw it }
            picked
        },
        runner = {
            runs++
            runnerFailure?.let { throw it }
            runnerResult
        },
        refresh = { refreshes++ },
        onFinished = { finished += it }
    )

    private fun start() = runBlocking { flow.start() }

    @Test
    fun cancellingThePickerDoesNothing() {
        picked = null
        start()
        assertEquals(0, runs)
        assertEquals(0, refreshes)
        assertTrue(finished.isEmpty())
    }

    @Test
    fun anImportedFileRefreshesTheScreensAndIsDeleted() {
        start()
        assertEquals(listOf(ImportResult.SUCCESS), finished)
        assertEquals(1, refreshes)
        verifySuspend { file.delete() }
    }

    @Test
    fun aFileThatIsNotRecognisedChangesNothing() {
        runnerResult = ImportResult.NOT_RECOGNIZED
        start()
        assertEquals(listOf(ImportResult.NOT_RECOGNIZED), finished)
        assertEquals(0, refreshes)
        verifySuspend { file.delete() }
    }

    @Test
    fun aFailedImportRefreshesBecauseTheHabitsWereReadAgain() {
        runnerResult = ImportResult.FAILED
        start()
        assertEquals(listOf(ImportResult.FAILED), finished)
        assertEquals(1, refreshes)
        verifySuspend { file.delete() }
    }

    @Test
    fun aRunnerThatThrowsIsAFailureAndTheFileIsStillDeleted() {
        runnerFailure = IllegalStateException("boom")
        start()
        assertEquals(listOf(ImportResult.FAILED), finished)
        verifySuspend { file.delete() }
    }

    @Test
    fun aPickerThatThrowsIsAFailureWithoutRefresh() {
        pickFailure = IllegalStateException("no window")
        start()
        assertEquals(listOf(ImportResult.FAILED), finished)
        assertEquals(0, runs)
        assertEquals(0, refreshes)
    }

    @Test
    fun aFileThatCannotBeDeletedStillReportsTheResult() {
        everySuspend { file.delete() } throws IllegalStateException("The file is locked")
        runnerResult = ImportResult.SUCCESS
        start()
        assertEquals(listOf(ImportResult.SUCCESS), finished)
    }
}
