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
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import io.github.crisacm.salem.core.models.Habit
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

/** The bar of the list drives [ListHabitsMenu] and [ListHabitsSelectionMenu]; their screens and adapters are mocks. */
@OptIn(ExperimentalTestApi::class)
class ListHabitsBarTest : BaseUiTest() {
    private val menuScreen = mock<ListHabitsMenuBehavior.Screen>()
    private val selectionScreen = mock<ListHabitsSelectionMenuBehavior.Screen>()
    private val selectionAdapter = mock<ListHabitsSelectionMenuBehavior.Adapter>()
    private lateinit var menu: ListHabitsMenu
    private lateinit var selection: ListHabitsSelectionMenu
    private lateinit var habit: Habit

    @Before
    override fun setUp() {
        super.setUp()
        val preferences = Preferences(MemoryStorage())
        habit = fixtures.createShortHabit()
        val menuBehavior = ListHabitsMenuBehavior(menuScreen, mock(), preferences, mock<ThemeSwitcher>())
        menu = ListHabitsMenu(preferences, mock<ThemeSwitcher>(), menuBehavior)
        val cache = HabitCardListCache(habitList, commandRunner, taskRunner, mock())
        val state = HabitListState(cache, preferences, mock<MidnightTimer>())
        val selectionBehavior = ListHabitsSelectionMenuBehavior(habitList, selectionScreen, selectionAdapter, commandRunner)
        selection = ListHabitsSelectionMenu(state, commandRunner, preferences, selectionBehavior, lazy { error("not used") }, mock())
        every { selectionAdapter.getSelected() } returns listOf(habit)
    }

    private fun androidx.compose.ui.test.ComposeUiTest.showBar() = setContent {
        MaterialTheme { ListHabitsBar(menu, selection) }
    }

    @Test
    fun theAddButtonAsksForTheTypeOfHabit() = runComposeUiTest {
        showBar()
        onNodeWithTag("actionCreateHabit").performClick()
        verify { menuScreen.showSelectHabitTypeDialog() }
    }

    @Test
    fun theOverflowMenuOpensTheSettings() = runComposeUiTest {
        showBar()
        onNodeWithTag("overflowMenu").performClick()
        onNodeWithText("Settings").performClick()
        verify { menuScreen.showSettingsScreen() }
    }

    @Test
    fun searchShowsTheFieldAndClosingGoesBackToTheTitle() = runComposeUiTest {
        showBar()
        onNodeWithTag("action_filter").performClick()
        onNodeWithText("Search").performClick()
        onNodeWithTag("searchField").assertIsDisplayed()
        assertTrue(menu.searchActive)
        onNodeWithTag("searchClose").performClick()
        assertFalse(menu.searchActive)
        onNodeWithTag("actionCreateHabit").assertIsDisplayed()
    }

    @Test
    fun theSelectionBarReplacesTheMainBarAndRunsItsActions() = runComposeUiTest {
        showBar()
        runOnIdle { selection.onSelectionStart() }
        onNodeWithTag("selectionCount").assertIsDisplayed()
        onNodeWithTag("action_edit_habit").performClick()
        verify { selectionScreen.showEditHabitsScreen(listOf(habit)) }
        onNodeWithTag("action_color").performClick()
        verify { selectionScreen.showColorPicker(habit.color, any()) }
    }
}
