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

package io.github.crisacm.salem.ui.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.HabitType
import org.junit.Test
import kotlin.test.assertEquals

/** The dialogs of the shared UI report what the user chose and leave closing to their host. */
@OptIn(ExperimentalTestApi::class)
class DialogContentTest {
    @Test
    fun choiceReportsTheIndexOfTheTappedOption() = runComposeUiTest {
        var chosen = -1
        setContent {
            MaterialTheme { ChoiceDialogContent(listOf("At least", "At most"), { chosen = it }, {}) }
        }
        onNodeWithText("At most").performClick()
        assertEquals(1, chosen)
    }

    @Test
    fun habitTypeReportsTheChosenType() = runComposeUiTest {
        var chosen: HabitType? = null
        setContent { MaterialTheme { HabitTypeDialogContent({ chosen = it }, {}) } }
        onNodeWithText("Yes or No").performClick()
        assertEquals(HabitType.YES_NO, chosen)
        onNodeWithText("Measurable").performClick()
        assertEquals(HabitType.NUMERICAL, chosen)
    }

    @Test
    fun confirmDeleteConfirmsOrDismisses() = runComposeUiTest {
        var confirmed = 0
        var dismissed = 0
        setContent {
            MaterialTheme { ConfirmDeleteDialogContent(2, { confirmed++ }, { dismissed++ }) }
        }
        onNodeWithText("Yes").performClick()
        onNodeWithText("No").performClick()
        assertEquals(1, confirmed)
        assertEquals(1, dismissed)
    }

    @Test
    fun frequencyPickerSavesTheSelectedMode() = runComposeUiTest {
        var picked: Pair<Int, Int>? = null
        setContent {
            MaterialTheme { FrequencyPickerContent(3, 7, { n, d -> picked = n to d }, {}) }
        }
        onNodeWithText("Save").performClick()
        assertEquals(3 to 7, picked)
    }

    @Test
    fun checkmarkOffersSkipOnlyWhenEnabled() = runComposeUiTest {
        val session = NotesSession("")
        val info = NumberSheetInfo("Meditate", "Today", "", Color.Blue, Color.White, Icons.Filled.Check)
        var chosen: Pair<Int, String>? = null
        setContent {
            MaterialTheme {
                CheckmarkPopup(session, info, Entry.NO, skipEnabled = true, unknownEnabled = false, { v, n -> chosen = v to n }, {})
            }
        }
        onNodeWithContentDescription("Skip").assertIsDisplayed()
        onNodeWithContentDescription("Yes").performClick()
        assertEquals(Entry.YES_MANUAL to "", chosen)
    }

    @Test
    fun numberSavesTheTypedValueWithTheNotes() = runComposeUiTest {
        val session = NotesSession("")
        val info = NumberSheetInfo("Run", "Today", "miles", Color.Blue, Color.White, Icons.Filled.Check)
        var saved: Pair<Double, String>? = null
        setContent {
            MaterialTheme {
                NumberPopup(session, info, 12.5, skipEnabled = false, unknownEnabled = false, '.', false, { v, n -> saved = v to n }, {})
            }
        }
        onNodeWithContentDescription("Save").performClick()
        assertEquals(12.5 to "", saved)
    }
}
