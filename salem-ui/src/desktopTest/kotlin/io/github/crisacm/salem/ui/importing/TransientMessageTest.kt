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

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import io.github.crisacm.salem.core.io.ImportResult
import io.github.crisacm.salem.ui.compose.MessageBanner
import io.github.crisacm.salem.ui.compose.TransientMessage
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.could_not_import
import io.github.crisacm.salem.ui.resources.file_not_recognized
import io.github.crisacm.salem.ui.resources.habits_imported
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TransientMessageTest {
    @Test
    fun theResultsHaveTheirMessage() {
        assertEquals(Res.string.habits_imported, importMessage(ImportResult.SUCCESS))
        assertEquals(Res.string.file_not_recognized, importMessage(ImportResult.NOT_RECOGNIZED))
        assertEquals(Res.string.could_not_import, importMessage(ImportResult.FAILED))
    }

    @Test
    fun aMessageShowsAndGoesAwayAfterAWhile() = runComposeUiTest {
        mainClock.autoAdvance = false
        val message = TransientMessage()
        setContent { MessageBanner(message) }
        onNodeWithText("Habits imported successfully.").assertDoesNotExist()
        message.show(Res.string.habits_imported)
        mainClock.advanceTimeByFrame()
        onNodeWithText("Habits imported successfully.").assertExists()
        mainClock.advanceTimeBy(2_500)
        onNodeWithText("Habits imported successfully.").assertExists()
        mainClock.advanceTimeBy(1_000)
        onNodeWithText("Habits imported successfully.").assertDoesNotExist()
    }

    @Test
    fun aNewMessageReplacesTheOldOneAndRestartsTheTime() = runComposeUiTest {
        mainClock.autoAdvance = false
        val message = TransientMessage()
        setContent { MessageBanner(message) }
        message.show(Res.string.habits_imported)
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeBy(2_000)
        message.show(Res.string.could_not_import)
        mainClock.advanceTimeByFrame()
        onNodeWithText("Habits imported successfully.").assertDoesNotExist()
        onNodeWithText("Failed to import data.").assertExists()
        mainClock.advanceTimeBy(2_000)
        onNodeWithText("Failed to import data.").assertExists()
        mainClock.advanceTimeBy(1_500)
        onNodeWithText("Failed to import data.").assertDoesNotExist()
    }
}
