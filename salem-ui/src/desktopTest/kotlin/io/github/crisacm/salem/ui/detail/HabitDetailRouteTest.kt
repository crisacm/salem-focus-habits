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

package io.github.crisacm.salem.ui.detail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.backhandler.LocalCompatNavigationEventDispatcherOwner
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import dev.mokkery.mock
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.memory.MemoryModelFactory
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.tasks.CoroutineTaskRunner
import io.github.crisacm.salem.core.test.HabitFixtures
import io.github.crisacm.salem.core.ui.screens.habits.edit.HabitEditorSaver
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.resetToday
import io.github.crisacm.salem.platform.time.setToday
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The whole detail as iOS shows it: the screen, the history editor, the popups and the messages over it. */
@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
class HabitDetailRouteTest {
    private val factory = MemoryModelFactory()
    private val habits = factory.buildHabitList()
    private val dispatcher = UnconfinedTestDispatcher()
    private val taskRunner = CoroutineTaskRunner(dispatcher, dispatcher)
    private val runner = CommandRunner(taskRunner)
    private val preferences = Preferences(MemoryStorage())
    private val fixtures = HabitFixtures(factory, habits)
    private var closes = 0
    private val navigationDispatcher = NavigationEventDispatcher()
    private val input = DirectNavigationEventInput().also { navigationDispatcher.addInput(it) }

    @Before
    fun fixToday() = setToday(LocalDate(2015, 1, 25))

    @After
    fun restoreToday() = resetToday()

    private fun androidx.compose.ui.test.ComposeUiTest.show(
        habit: Habit = fixtures.createShortHabit(),
        theme: () -> Theme = { LightTheme() }
    ): Habit {
        habits.add(habit)
        setContent {
            val owner = object : NavigationEventDispatcherOwner {
                override val navigationEventDispatcher = navigationDispatcher
            }
            CompositionLocalProvider(LocalCompatNavigationEventDispatcherOwner provides owner) {
                MaterialTheme {
                    HabitDetailRoute(
                        habit = habit,
                        theme = theme(),
                        dateFormatter = JavaLocalDateFormatter(Locale.US),
                        habitList = habits,
                        preferences = preferences,
                        commandRunner = runner,
                        taskRunner = taskRunner,
                        system = mock<ShowHabitMenuPresenter.System>(),
                        saverFactory = { HabitEditorSaver(factory, habits, runner) },
                        decimalSeparator = '.',
                        onClose = { closes++ }
                    )
                }
            }
        }
        waitForIdle()
        return habit
    }

    private fun sendBack() {
        input.backStarted(NavigationEvent(NavigationEvent.EDGE_NONE, 0f, 0f, 0f, 0L))
        input.backCompleted()
    }

    @Test
    fun theDetailOfTheHabitIsShown() = runComposeUiTest {
        val habit = show()
        onNodeWithTag("showHabitScreen").assertIsDisplayed()
        onNodeWithText(habit.name).assertIsDisplayed()
    }

    @Test
    fun editOpensTheEditorAndBackClosesItBeforeTheDetail() = runComposeUiTest {
        show()
        onNodeWithTag("action_edit_habit").performClick()
        onNodeWithTag("nameInput").assertIsDisplayed()
        runOnIdle { sendBack() }
        onNodeWithTag("nameInput").assertDoesNotExist()
        assertEquals(0, closes)
    }

    @Test
    fun theHistoryEditorOpensAndBackClosesItBeforeTheDetail() = runComposeUiTest {
        show()
        onNodeWithTag("historyEdit").performScrollTo().performClick()
        onNodeWithTag("historyEditorChart").assertIsDisplayed()
        runOnIdle { sendBack() }
        onNodeWithTag("historyEditorChart").assertDoesNotExist()
        assertEquals(0, closes)
    }

    @Test
    fun aDayOfTheHistoryEditorOpensAPopupAboveItAndClosingThePopupKeepsTheEditor() = runComposeUiTest {
        show()
        onNodeWithTag("historyEdit").performScrollTo().performClick()
        onNodeWithTag("historyEditorChart").performTouchInput { click(center) }
        waitForIdle()
        onNodeWithContentDescription("Yes").assertIsDisplayed()
        runOnIdle { sendBack() }
        onNodeWithContentDescription("Yes").assertDoesNotExist()
        onNodeWithTag("historyEditorChart").assertIsDisplayed()
        assertEquals(0, closes)
    }

    @Test
    fun archivingShowsAMessageAndTheMenuOffersUnarchive() = runComposeUiTest {
        mainClock.autoAdvance = false
        show()
        onNodeWithTag("overflowMenu").performClick()
        mainClock.advanceTimeByFrame()
        onNodeWithTag("action_archive_habit").performClick()
        mainClock.advanceTimeByFrame()
        onNodeWithText("Habit archived").assertIsDisplayed()
        onNodeWithTag("overflowMenu").performClick()
        mainClock.advanceTimeByFrame()
        onNodeWithTag("action_unarchive_habit").assertExists()
        onNodeWithTag("action_archive_habit").assertDoesNotExist()
    }

    @Test
    fun deletingAsksForConfirmationAndThenLeavesTheDetail() = runComposeUiTest {
        val habit = show()
        onNodeWithTag("overflowMenu").performClick()
        onNodeWithTag("action_delete").performClick()
        assertEquals(0, closes)
        onNodeWithText("Yes").performClick()
        waitForIdle()
        assertEquals(1, closes)
        assertEquals(null, habits.getById(habit.id!!))
    }

    @Test
    fun exportIsNotAvailableAndWritesNothing() = runComposeUiTest {
        mainClock.autoAdvance = false
        show()
        onNodeWithTag("overflowMenu").performClick()
        mainClock.advanceTimeByFrame()
        onNodeWithTag("export").performClick()
        mainClock.advanceTimeByFrame()
        onNodeWithText("Exporting is not available on iOS yet").assertIsDisplayed()
        assertEquals(0, closes)
    }

    @Test
    fun backWithNothingOpenLeavesTheDetail() = runComposeUiTest {
        show()
        runOnIdle { sendBack() }
        assertEquals(1, closes)
    }

    @Test
    fun theBackButtonOfTheBarLeavesTheDetail() = runComposeUiTest {
        show()
        onNodeWithTag("showHabitBack").performClick()
        assertEquals(1, closes)
    }

    @Test
    fun randomizeIsHiddenUnlessTheHostAllowsIt() = runComposeUiTest {
        show()
        onNodeWithTag("overflowMenu").performClick()
        onNodeWithTag("action_randomize").assertDoesNotExist()
    }

    @Test
    fun aNewThemeRedrawsTheCardsWithIt() = runComposeUiTest {
        var theme by mutableStateOf<Theme>(LightTheme())
        show(theme = { theme })
        fun background() = onNodeWithTag("showHabitCards").captureToImage().toPixelMap()[2, 2].luminance()
        assertTrue(background() > 0.8, "the light theme is light: ${background()}")
        runOnIdle { theme = DarkTheme() }
        waitForIdle()
        assertTrue(background() < 0.4, "the dark theme is dark: ${background()}")
    }

    @Test
    fun theSameThemeAsANewInstanceDoesNotRefreshAgain() = runComposeUiTest {
        var theme by mutableStateOf<Theme>(LightTheme())
        show(theme = { theme })
        runOnIdle { theme = LightTheme() }
        waitForIdle()
        onNodeWithTag("showHabitScreen").assertIsDisplayed()
    }
}
