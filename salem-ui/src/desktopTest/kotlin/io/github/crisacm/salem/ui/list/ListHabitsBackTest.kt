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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.backhandler.LocalCompatNavigationEventDispatcherOwner
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.core.ui.screens.habits.list.HabitCardListCache
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsMenuBehavior
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior
import io.github.crisacm.salem.core.utils.MidnightTimer
import io.github.crisacm.salem.ui.BaseUiTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
class ListHabitsBackTest : BaseUiTest() {
    private lateinit var menu: ListHabitsMenu
    private lateinit var selection: ListHabitsSelectionMenu
    private val dispatcher = NavigationEventDispatcher()
    private val input = DirectNavigationEventInput()

    @Before
    override fun setUp() {
        super.setUp()
        dispatcher.addInput(input)
        val preferences = Preferences(MemoryStorage())
        menu = ListHabitsMenu(
            preferences,
            mock<ThemeSwitcher>(),
            ListHabitsMenuBehavior(mock(), mock(), preferences, mock<ThemeSwitcher>())
        )
        val adapter = mock<ListHabitsSelectionMenuBehavior.Adapter>()
        every { adapter.getSelected() } returns listOf(fixtures.createShortHabit())
        val state = HabitListState(HabitCardListCache(habitList, commandRunner, taskRunner, mock()), preferences, mock<MidnightTimer>())
        val controller = HabitListController(state, mock(), lazy { selection })
        selection = ListHabitsSelectionMenu(
            state,
            commandRunner,
            preferences,
            ListHabitsSelectionMenuBehavior(habitList, mock(), adapter, commandRunner),
            lazy { controller },
            mock()
        )
    }

    // A back gesture of the platform, sent to the dispatcher that the bar listens to
    private fun sendBack() {
        input.backStarted(NavigationEvent(NavigationEvent.EDGE_NONE, 0f, 0f, 0f, 0L))
        input.backCompleted()
    }

    private fun androidx.compose.ui.test.ComposeUiTest.showBar() = setContent {
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = dispatcher
        }
        CompositionLocalProvider(LocalCompatNavigationEventDispatcherOwner provides owner) {
            MaterialTheme { ListHabitsBar(menu, selection, Color.Blue, Color.White) }
        }
    }

    @Test
    fun backClosesTheSearch() = runComposeUiTest {
        showBar()
        runOnIdle { menu.startSearch() }
        onNodeWithTag("searchField").assertIsDisplayed()
        runOnIdle { sendBack() }
        runOnIdle { assertFalse(menu.searchActive) }
        onNodeWithTag("actionCreateHabit").assertIsDisplayed()
    }

    @Test
    fun backEndsTheSelectionBeforeTheSearch() = runComposeUiTest {
        showBar()
        runOnIdle {
            menu.startSearch()
            selection.onSelectionStart()
        }
        onNodeWithTag("selectionCount").assertIsDisplayed()
        runOnIdle { sendBack() }
        // The selection is cancelled through the controller; the search stays until the next back
        runOnIdle {
            assertFalse(selection.active)
            assertTrue(menu.searchActive)
        }
    }
}
