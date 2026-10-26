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

import dev.mokkery.mock
import io.github.crisacm.salem.core.commands.CreateRepetitionCommand
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.ui.BaseUiTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitDetailSessionTest : BaseUiTest() {
    private val hooks = RecordingDetailHooks()
    private val preferences = Preferences(MemoryStorage())
    private lateinit var habit: Habit
    private lateinit var session: HabitDetailSession

    @Before
    override fun setUp() {
        super.setUp()
        habit = fixtures.createShortHabit()
        habitList.add(habit)
        session = HabitDetailSession(
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
    }

    @Test
    fun theStateIsEmptyUntilTheFirstRefresh() {
        assertNull(session.state)
    }

    @Test
    fun attachingBuildsTheStateOfTheHabit() {
        session.attach()
        val state = assertNotNull(session.state)
        assertEquals(habit.name, state.title)
        assertEquals(1, hooks.states.size)
        session.detach()
    }

    @Test
    fun aCommandRefreshesTheStateWhileAttached() {
        session.attach()
        val before = hooks.states.size
        commandRunner.run(CreateRepetitionCommand(habitList, habit, getToday(), Entry.YES_MANUAL, ""))
        assertTrue(hooks.states.size > before, "the state was built again")
        session.detach()
    }

    @Test
    fun aCommandDoesNotRefreshAfterDetaching() {
        session.attach()
        session.detach()
        val before = hooks.states.size
        commandRunner.run(CreateRepetitionCommand(habitList, habit, getToday(), Entry.YES_MANUAL, ""))
        assertEquals(before, hooks.states.size)
    }

    @Test
    fun archivingTurnsArchiveIntoUnarchive() {
        session.attach()
        assertTrue(session.canArchive)
        assertFalse(session.canUnarchive)
        session.menu.onArchiveHabits()
        assertFalse(session.canArchive)
        assertTrue(session.canUnarchive)
        assertEquals(listOf<ShowHabitMenuPresenter.Message?>(ShowHabitMenuPresenter.Message.HABIT_ARCHIVED), hooks.messages)
        session.menu.onUnarchiveHabits()
        assertTrue(session.canArchive)
        assertFalse(session.canUnarchive)
        session.detach()
    }

    @Test
    fun deletingAsksForConfirmationAndOnlyThenDeletesAndCloses() {
        session.attach()
        session.menu.onDeleteHabit()
        assertNotNull(hooks.confirmation)
        assertEquals(1, habitList.size())
        assertEquals(0, hooks.closed)
        hooks.confirmation!!.onConfirmed()
        assertEquals(0, habitList.size())
        assertEquals(1, hooks.closed)
        session.detach()
    }

    @Test
    fun editingOpensTheEditorOfTheHabit() {
        session.menu.onEditHabit()
        assertEquals(habit, hooks.edited)
    }

    @Test
    fun theScoreSpinnerSavesThePositionAndUpdatesTheWidgets() {
        session.attach()
        session.callbacks.onScoreSpinner(3)
        assertEquals(3, preferences.scoreCardSpinnerPosition)
        assertEquals(1, hooks.widgetUpdates)
        assertEquals(3, session.state!!.scores.spinnerPosition)
        session.detach()
    }

    @Test
    fun theHistoryEditButtonOpensTheHistoryEditor() {
        session.callbacks.onHistoryEdit()
        assertNotNull(hooks.historyEditorListener)
    }

    @Test
    fun developerOptionsFollowThePreferences() {
        assertFalse(session.isDeveloper)
        preferences.isDeveloper = true
        val developer = HabitDetailSession(
            habit, habitList, preferences, commandRunner, taskRunner, mock<ShowHabitMenuPresenter.System>(),
            { LightTheme() }, hooks, CoroutineScope(UnconfinedTestDispatcher())
        )
        assertTrue(developer.isDeveloper)
    }

    @Test
    fun developerOptionsCanBeGivenApartFromThePreferences() {
        val developer = HabitDetailSession(
            habit, habitList, preferences, commandRunner, taskRunner, mock<ShowHabitMenuPresenter.System>(),
            { LightTheme() }, hooks, CoroutineScope(UnconfinedTestDispatcher()), developerOptions = true
        )
        assertTrue(developer.isDeveloper)
        assertFalse(preferences.isDeveloper)
    }

    @Test
    fun attachingTwiceRegistersTheSessionOnlyOnce() {
        session.attach()
        session.attach()
        val before = hooks.states.size
        commandRunner.run(CreateRepetitionCommand(habitList, habit, getToday(), Entry.YES_MANUAL, ""))
        assertEquals(before + 1, hooks.states.size)
        session.detach()
    }

    @Test
    fun oneDetachStopsASessionThatWasAttachedTwice() {
        session.attach()
        session.attach()
        session.detach()
        val before = hooks.states.size
        commandRunner.run(CreateRepetitionCommand(habitList, habit, getToday(), Entry.YES_MANUAL, ""))
        assertEquals(before, hooks.states.size)
    }

    @Test
    fun aRefreshQueuedBeforeDetachingDoesNotRunAfterIt() {
        val dispatcher = StandardTestDispatcher()
        val queued = HabitDetailSession(
            habit, habitList, preferences, commandRunner, taskRunner, mock<ShowHabitMenuPresenter.System>(),
            { LightTheme() }, hooks, CoroutineScope(dispatcher)
        )
        queued.attach()
        queued.detach()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(hooks.states.isEmpty(), "the queued refresh was dropped")
        assertNull(queued.state)
    }

    @Test
    fun aRefreshAskedWhileDetachedDoesNothing() {
        session.attach()
        session.detach()
        val before = hooks.states.size
        session.refresh()
        assertEquals(before, hooks.states.size)
    }
}
