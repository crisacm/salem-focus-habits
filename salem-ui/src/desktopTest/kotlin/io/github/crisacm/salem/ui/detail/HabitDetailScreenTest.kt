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
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.mokkery.mock
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.ui.BaseUiTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class HabitDetailScreenTest : BaseUiTest() {
    private val hooks = RecordingDetailHooks()
    private val preferences = Preferences(MemoryStorage())
    private var backs = 0

    private fun sessionFor(habit: Habit) = HabitDetailSession(
        habit = habit,
        habitList = habitList,
        preferences = preferences,
        commandRunner = commandRunner,
        taskRunner = taskRunner,
        system = mock<ShowHabitMenuPresenter.System>(),
        theme = { LightTheme() },
        hooks = hooks,
        scope = CoroutineScope(UnconfinedTestDispatcher())
    )

    private fun androidx.compose.ui.test.ComposeUiTest.show(habit: Habit): HabitDetailSession {
        habitList.add(habit)
        val session = sessionFor(habit)
        session.attach()
        setContent {
            MaterialTheme {
                HabitDetailScreen(session, JavaLocalDateFormatter(Locale.US), onBack = { backs++ })
            }
        }
        return session
    }

    @Test
    fun aYesOrNoHabitShowsTheTitleAndItsCards() = runComposeUiTest {
        val habit = fixtures.createShortHabit()
        show(habit)
        onNodeWithTag("showHabitScreen").assertExists()
        onNodeWithText(habit.name).assertIsDisplayed()
        for (tag in listOf("subtitleCard", "overviewCard", "scoreCard", "barCard", "historyCard", "streakCard", "frequencyCard")) {
            onNodeWithTag(tag).assertExists()
        }
        onNodeWithTag("targetCard").assertDoesNotExist()
        onNodeWithTag("notesCard").assertDoesNotExist()
    }

    @Test
    fun aNumericalHabitShowsTheTargetCardInsteadOfTheOverview() = runComposeUiTest {
        show(fixtures.createNumericalHabit())
        onNodeWithTag("targetCard").assertExists()
        onNodeWithTag("overviewCard").assertDoesNotExist()
    }

    @Test
    fun theNotesCardAppearsWithADescription() = runComposeUiTest {
        val habit = fixtures.createShortHabit().also { it.description = "Remember to breathe" }
        show(habit)
        onNodeWithTag("notesCard").assertExists()
    }

    @Test
    fun theBackButtonLeavesTheScreen() = runComposeUiTest {
        show(fixtures.createShortHabit())
        onNodeWithTag("showHabitBack").performClick()
        assertEquals(1, backs)
    }

    @Test
    fun theEditButtonOpensTheEditorOfTheHabit() = runComposeUiTest {
        val habit = fixtures.createShortHabit()
        show(habit)
        onNodeWithTag("action_edit_habit").performClick()
        assertEquals(habit, hooks.edited)
    }

    @Test
    fun theMenuOffersArchiveAndThenUnarchive() = runComposeUiTest {
        show(fixtures.createShortHabit())
        onNodeWithTag("overflowMenu").performClick()
        onNodeWithTag("export").assertExists()
        onNodeWithTag("action_delete").assertExists()
        onNodeWithTag("action_unarchive_habit").assertDoesNotExist()
        onNodeWithTag("action_archive_habit").performClick()
        waitForIdle()

        onNodeWithTag("overflowMenu").performClick()
        onNodeWithTag("action_unarchive_habit").assertExists()
        onNodeWithTag("action_archive_habit").assertDoesNotExist()
    }

    @Test
    fun deletingAsksForConfirmation() = runComposeUiTest {
        show(fixtures.createShortHabit())
        onNodeWithTag("overflowMenu").performClick()
        onNodeWithTag("action_delete").performClick()
        assertNotNull(hooks.confirmation)
    }

    @Test
    fun randomizeIsOnlyForDevelopers() = runComposeUiTest {
        show(fixtures.createShortHabit())
        onNodeWithTag("overflowMenu").performClick()
        onNodeWithTag("action_randomize").assertDoesNotExist()
    }

    @Test
    fun aDeveloperSeesRandomize() = runComposeUiTest {
        preferences.isDeveloper = true
        show(fixtures.createShortHabit())
        onNodeWithTag("overflowMenu").performClick()
        onNodeWithTag("action_randomize").assertExists()
    }

    @Test
    fun theScreenFillsTheAreaBeforeTheStateExists() = runComposeUiTest {
        val habit = fixtures.createShortHabit()
        habitList.add(habit)
        val session = sessionFor(habit)
        setContent {
            MaterialTheme {
                HabitDetailScreen(session, JavaLocalDateFormatter(Locale.US), onBack = { backs++ })
            }
        }
        onNodeWithTag("showHabitScreen").assertExists()
        onNodeWithTag("showHabitBack").assertDoesNotExist()
    }
}
