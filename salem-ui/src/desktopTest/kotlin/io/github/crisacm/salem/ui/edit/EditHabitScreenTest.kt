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

package io.github.crisacm.salem.ui.edit

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.backhandler.LocalCompatNavigationEventDispatcherOwner
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
class EditHabitScreenTest {
    private class FakeFormatter : LocalDateFormatter {
        override fun narrowWeekdayName(weekday: DayOfWeek) = "n"
        override fun narrowWeekdayName(date: LocalDate) = "n"
        override fun shortWeekdayName(weekday: DayOfWeek) = "short"
        override fun shortWeekdayName(date: LocalDate) = "short"
        override fun shortMonthName(date: LocalDate) = "month"
        override fun longWeekdayName(weekday: DayOfWeek) = "long"
        override fun longMonthName(date: LocalDate) = "long month"
    }

    private val dispatcher = NavigationEventDispatcher()
    private val input = DirectNavigationEventInput().also { dispatcher.addInput(it) }
    private var saved: EditHabitForm? = null
    private var backs = 0

    private fun androidx.compose.ui.test.ComposeUiTest.show(initial: EditHabitForm = EditHabitForm.new(HabitType.YES_NO)) =
        setContent {
            val owner = object : NavigationEventDispatcherOwner {
                override val navigationEventDispatcher = dispatcher
            }
            CompositionLocalProvider(LocalCompatNavigationEventDispatcherOwner provides owner) {
                MaterialTheme {
                    EditHabitScreen(
                        initial = initial,
                        isEditing = false,
                        theme = LightTheme(),
                        isNight = false,
                        dateFormatter = FakeFormatter(),
                        onSave = { saved = it },
                        onBack = { backs++ }
                    )
                }
            }
        }

    private fun sendBack() {
        input.backStarted(NavigationEvent(NavigationEvent.EDGE_NONE, 0f, 0f, 0f, 0L))
        input.backCompleted()
    }

    @Test
    fun aBlankNameBlocksTheSaveAndShowsTheError() = runComposeUiTest {
        show()
        onNodeWithTag("buttonSave").performClick()
        onNodeWithText("Cannot be blank").assertIsDisplayed()
        assertNull(saved)
    }

    @Test
    fun aValidFormIsReportedOnSave() = runComposeUiTest {
        show()
        onNodeWithTag("nameInput").performTextInput("Meditate")
        onNodeWithTag("buttonSave").performClick()
        assertEquals("Meditate", saved?.name)
    }

    @Test
    fun aNumericalHabitNeedsATarget() = runComposeUiTest {
        show(EditHabitForm.new(HabitType.NUMERICAL))
        onNodeWithTag("nameInput").performTextInput("Run")
        onNodeWithTag("buttonSave").performClick()
        assertNull(saved)
        onNodeWithTag("targetInput").performTextInput("5")
        onNodeWithTag("buttonSave").performClick()
        assertEquals("Run", saved?.name)
    }

    @Test
    fun theColourPickerChangesTheColourOfTheForm() = runComposeUiTest {
        show()
        onNodeWithTag("colorButton").performClick()
        onNodeWithText("Change color").assertIsDisplayed()
        onNodeWithContentDescription("Color 1").performClick()
        onNodeWithTag("buttonSave").assertIsDisplayed()
        onNodeWithTag("nameInput").performTextInput("Meditate")
        onNodeWithTag("buttonSave").performClick()
        assertEquals(PaletteColor(0), saved?.color)
    }

    @Test
    fun theReminderTimeAddsTheDaysRow() = runComposeUiTest {
        show()
        onNodeWithTag("reminderTimePicker").performClick()
        onNodeWithTag("timePickerOk").performClick()
        onNodeWithTag("reminderDatePicker").assertIsDisplayed()
        onNodeWithTag("nameInput").performTextInput("Meditate")
        onNodeWithTag("buttonSave").performClick()
        assertEquals(8 to 0, saved?.let { it.reminderHour to it.reminderMin })
    }

    @Test
    fun theFrequencyPickerClosesOnSave() = runComposeUiTest {
        show()
        onNodeWithTag("frequencyPicker").performClick()
        onNodeWithTag("buttonSave").assertDoesNotExist()
        onNodeWithText("Save").performClick()
        onNodeWithTag("buttonSave").assertIsDisplayed()
    }

    @Test
    fun theFormIsHiddenFromAccessibilityWhileAPickerIsOpen() = runComposeUiTest {
        show()
        onNodeWithTag("colorButton").performClick()
        onNodeWithTag("nameInput").assertDoesNotExist()
        onNodeWithTag("buttonSave").assertDoesNotExist()
    }

    @Test
    fun backClosesThePickerBeforeLeavingTheScreen() = runComposeUiTest {
        show()
        onNodeWithTag("colorButton").performClick()
        onNodeWithText("Change color").assertIsDisplayed()
        runOnIdle { sendBack() }
        onNodeWithTag("buttonSave").assertIsDisplayed()
        assertEquals(0, backs)
    }
}
