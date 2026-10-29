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

package io.github.crisacm.salem.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.LocalCompatNavigationEventDispatcherOwner
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.memory.MemoryModelFactory
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.tasks.CoroutineTaskRunner
import io.github.crisacm.salem.core.test.HabitFixtures
import io.github.crisacm.salem.core.ui.callbacks.CheckMarkDialogCallback
import io.github.crisacm.salem.core.ui.callbacks.NumberPickerCallback
import io.github.crisacm.salem.core.ui.callbacks.OnColorPickedCallback
import io.github.crisacm.salem.core.ui.callbacks.OnConfirmedCallback
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm
import io.github.crisacm.salem.core.ui.screens.habits.edit.HabitEditorSaver
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.platform.time.resetToday
import io.github.crisacm.salem.platform.time.setToday
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
class ListOverlayHostTest {
    private class FakeFormatter : LocalDateFormatter {
        override fun narrowWeekdayName(weekday: DayOfWeek) = "n"
        override fun narrowWeekdayName(date: LocalDate) = "n"
        override fun shortWeekdayName(weekday: DayOfWeek) = "short"
        override fun shortWeekdayName(date: LocalDate) = "short"
        override fun shortMonthName(date: LocalDate) = "month"
        override fun longWeekdayName(weekday: DayOfWeek) = "long"
        override fun longMonthName(date: LocalDate) = "long month"
        override fun longFormat(date: LocalDate) = "long date"
    }

    private val factory = MemoryModelFactory()
    private val habits = factory.buildHabitList()
    private val runner = CommandRunner(CoroutineTaskRunner(UnconfinedTestDispatcher(), UnconfinedTestDispatcher()))
    private val overlays = ListOverlays()
    private var saver: () -> HabitEditorSaver = { HabitEditorSaver(factory, habits, runner) }
    private val dispatcher = NavigationEventDispatcher()
    private val input = DirectNavigationEventInput().also { dispatcher.addInput(it) }

    private fun sampleHabit() = HabitFixtures(factory, habits).createShortHabit().also { habits.add(it) }

    @Before
    fun fixToday() = setToday(LocalDate(2015, 1, 25))

    @After
    fun restoreToday() = resetToday()

    private fun androidx.compose.ui.test.ComposeUiTest.show() = setContent {
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = dispatcher
        }
        CompositionLocalProvider(LocalCompatNavigationEventDispatcherOwner provides owner) {
            MaterialTheme {
                ListOverlayHost(
                    overlays = overlays,
                    theme = LightTheme(),
                    dateFormatter = FakeFormatter(),
                    preferences = Preferences(MemoryStorage()),
                    habitList = habits,
                    saverFactory = { saver() },
                    decimalSeparator = '.'
                )
            }
        }
    }

    private fun sendBack() {
        input.backStarted(NavigationEvent(NavigationEvent.EDGE_NONE, 0f, 0f, 0f, 0L))
        input.backCompleted()
    }

    @Test
    fun nothingIsShownWithoutAnOverlay() = runComposeUiTest {
        show()
        onNodeWithTag("buttonSave").assertDoesNotExist()
        assertNull(overlays.current)
    }

    @Test
    fun theHabitTypeDialogOpensTheEditorForTheChosenType() = runComposeUiTest {
        show()
        runOnIdle { overlays.showHabitType() }
        onNodeWithText("Yes or No").performClick()
        onNodeWithTag("nameInput").assertIsDisplayed()
        onNodeWithTag("frequencyPicker").assertIsDisplayed()
    }

    @Test
    fun savingANewHabitCreatesItAndClosesTheEditor() = runComposeUiTest {
        show()
        runOnIdle { overlays.showEditor(null, HabitType.YES_NO) }
        onNodeWithTag("nameInput").performTextInput("Meditate")
        onNodeWithTag("buttonSave").performClick()
        runOnIdle {
            assertEquals(1, habits.size())
            assertEquals("Meditate", habits.getByPosition(0).name)
            assertNull(overlays.current)
        }
    }

    @Test
    fun editingAHabitStartsFromItsValuesAndUpdatesIt() = runComposeUiTest {
        val habit = HabitFixtures(factory, habits).createShortHabit()
        habits.add(habit)
        show()
        runOnIdle { overlays.showEditor(habit.id, habit.type) }
        onNodeWithText("Wake up early").assertIsDisplayed()
        onNodeWithTag("nameInput").performTextReplacement("Renamed")
        onNodeWithTag("buttonSave").performClick()
        runOnIdle {
            assertEquals("Renamed", habits.getById(habit.id!!)!!.name)
            assertNull(overlays.current)
        }
    }

    @Test
    fun anEditorForAMissingHabitClosesItself() = runComposeUiTest {
        show()
        runOnIdle { overlays.showEditor(12345L, HabitType.YES_NO) }
        waitForIdle()
        onNodeWithTag("buttonSave").assertDoesNotExist()
        assertNull(overlays.current)
    }

    @Test
    fun confirmingTheDeletionCallsBackAndCloses() = runComposeUiTest {
        var confirmed = 0
        show()
        runOnIdle { overlays.showDelete(2, OnConfirmedCallback { confirmed++ }) }
        onNodeWithText("Yes").performClick()
        runOnIdle {
            assertEquals(1, confirmed)
            assertNull(overlays.current)
        }
    }

    @Test
    fun cancellingTheDeletionDoesNotCallBack() = runComposeUiTest {
        var confirmed = 0
        show()
        runOnIdle { overlays.showDelete(2, OnConfirmedCallback { confirmed++ }) }
        onNodeWithText("No").performClick()
        runOnIdle {
            assertEquals(0, confirmed)
            assertNull(overlays.current)
        }
    }

    @Test
    fun theColourPickerReportsThePickedColour() = runComposeUiTest {
        var picked: PaletteColor? = null
        show()
        runOnIdle { overlays.showColor(PaletteColor(3), OnColorPickedCallback { picked = it }) }
        onNodeWithContentDescription("Color 4 selected").assertIsDisplayed()
        onNodeWithContentDescription("Color 1").performClick()
        runOnIdle {
            assertEquals(PaletteColor(0), picked)
            assertNull(overlays.current)
        }
    }

    @Test
    fun theCheckmarkPopupReportsTheValueAndTheNotes() = runComposeUiTest {
        var result: Pair<Int, String>? = null
        show()
        runOnIdle {
            overlays.showCheckmark(Entry.NO, "", PaletteColor(2), CheckMarkDialogCallback { v, n -> result = v to n })
        }
        onNodeWithContentDescription("Yes").performClick()
        runOnIdle {
            assertEquals(Entry.YES_MANUAL to "", result)
            assertNull(overlays.current)
        }
    }

    @Test
    fun closingACheckmarkPopupWithNewNotesSavesThemWithTheOriginalValue() = runComposeUiTest {
        var result: Pair<Int, String>? = null
        show()
        runOnIdle {
            overlays.showCheckmark(Entry.YES_MANUAL, "", PaletteColor(2), CheckMarkDialogCallback { v, n -> result = v to n })
        }
        onNode(hasSetTextAction()).performTextInput("late")
        runOnIdle { sendBack() }
        runOnIdle {
            assertEquals(Entry.YES_MANUAL to "late", result)
            assertNull(overlays.current)
        }
    }

    @Test
    fun closingACheckmarkPopupWithoutChangesReportsNothing() = runComposeUiTest {
        var result: Pair<Int, String>? = null
        show()
        runOnIdle {
            overlays.showCheckmark(Entry.NO, "", PaletteColor(2), CheckMarkDialogCallback { v, n -> result = v to n })
        }
        runOnIdle { sendBack() }
        runOnIdle {
            assertNull(result)
            assertNull(overlays.current)
        }
    }

    @Test
    fun theNumberPopupSavesTheTypedValue() = runComposeUiTest {
        var result: Pair<Double, String>? = null
        show()
        runOnIdle { overlays.showNumber(sampleHabit(), LocalDate(2015, 1, 25), 12.5, "", NumberPickerCallback { v, n -> result = v to n }) }
        onNodeWithContentDescription("Save").performClick()
        runOnIdle {
            assertEquals(12.5 to "", result)
            assertNull(overlays.current)
        }
    }

    @Test
    fun backClosesTheDeletionWithoutConfirming() = runComposeUiTest {
        var confirmed = 0
        show()
        runOnIdle { overlays.showDelete(1, OnConfirmedCallback { confirmed++ }) }
        runOnIdle { sendBack() }
        runOnIdle {
            assertEquals(0, confirmed)
            assertNull(overlays.current)
        }
    }

    @Test
    fun showingAnotherOverlayReplacesTheCurrentOne() = runComposeUiTest {
        show()
        runOnIdle {
            overlays.showHabitType()
            overlays.showDelete(1, OnConfirmedCallback { })
        }
        assertNotNull(overlays.current)
        assertTrue(overlays.current is ListOverlay.Delete)
    }

    @Test
    fun aStaleDismissDoesNotCloseANewerOverlay() {
        overlays.showHabitType()
        val first = overlays.current!!
        overlays.showDelete(1, OnConfirmedCallback { })
        overlays.dismiss(first)
        assertTrue(overlays.current is ListOverlay.Delete)
    }

    @Test
    fun dismissingTheCurrentOverlayClosesIt() {
        overlays.showHabitType()
        overlays.dismiss(overlays.current!!)
        assertNull(overlays.current)
    }

    @Test
    fun theListIsHiddenFromAccessibilityWhileAnOverlayIsOpen() = runComposeUiTest {
        setContent { Box(Modifier.hiddenWhileOverlay(overlays)) { Text("row", Modifier.testTag("row")) } }
        onNodeWithTag("row").assertIsDisplayed()
        runOnIdle { overlays.showHabitType() }
        onNodeWithTag("row").assertDoesNotExist()
        runOnIdle { overlays.dismiss(overlays.current!!) }
        onNodeWithTag("row").assertIsDisplayed()
    }

    @Test
    fun backClosesOnlyThePickerOfTheEditorFirst() = runComposeUiTest {
        show()
        runOnIdle { overlays.showEditor(null, HabitType.YES_NO) }
        onNodeWithTag("colorButton").performClick()
        onNodeWithText("Change color").assertIsDisplayed()
        runOnIdle { sendBack() }
        onNodeWithTag("nameInput").assertIsDisplayed()
        runOnIdle { assertTrue(overlays.current is ListOverlay.Editor) }
        runOnIdle { sendBack() }
        runOnIdle { assertNull(overlays.current) }
    }

    @Test
    fun backFromTheNumberPopupWithNewNotesSavesThemWithTheOriginalValue() = runComposeUiTest {
        var result: Pair<Double, String>? = null
        show()
        runOnIdle { overlays.showNumber(sampleHabit(), LocalDate(2015, 1, 25), 7.5, "", NumberPickerCallback { v, n -> result = v to n }) }
        // The notes are the second field of the sheet, after the value
        onAllNodes(hasSetTextAction())[1].performTextInput("late")
        runOnIdle { sendBack() }
        runOnIdle {
            assertEquals(7.5 to "late", result)
            assertNull(overlays.current)
        }
    }

    @Test
    fun anEditorWhoseSaverAlreadySavedStaysOpen() = runComposeUiTest {
        val used = HabitEditorSaver(factory, habits, runner)
        used.save(EditHabitForm.new(HabitType.YES_NO).copy(name = "First"), null)
        saver = { used }
        show()
        runOnIdle { overlays.showEditor(null, HabitType.YES_NO) }
        onNodeWithTag("nameInput").performTextInput("Second")
        onNodeWithTag("buttonSave").performClick()
        runOnIdle {
            assertTrue(overlays.current is ListOverlay.Editor)
            assertEquals(1, habits.size())
        }
    }

    @Test
    fun aTapOutsideACheckmarkPopupSavesChangedNotesOnce() = runComposeUiTest {
        val results = mutableListOf<Pair<Int, String>>()
        show()
        runOnIdle {
            overlays.showCheckmark(Entry.YES_MANUAL, "", PaletteColor(2), CheckMarkDialogCallback { v, n -> results += v to n })
        }
        onNode(hasSetTextAction()).performTextInput("late")
        onRoot().performTouchInput { click(Offset(2f, 2f)) }
        runOnIdle {
            assertEquals(listOf(Entry.YES_MANUAL to "late"), results)
            assertNull(overlays.current)
        }
    }

    @Test
    fun aTapOutsideTheNumberPopupSavesChangedNotesOnce() = runComposeUiTest {
        val results = mutableListOf<Pair<Double, String>>()
        show()
        runOnIdle { overlays.showNumber(sampleHabit(), LocalDate(2015, 1, 25), 7.5, "", NumberPickerCallback { v, n -> results += v to n }) }
        onAllNodes(hasSetTextAction())[1].performTextInput("late")
        onRoot().performTouchInput { click(Offset(2f, 2f)) }
        runOnIdle {
            assertEquals(listOf(7.5 to "late"), results)
            assertNull(overlays.current)
        }
    }
}
