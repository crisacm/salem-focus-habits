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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import dev.mokkery.mock
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.tasks.Task
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.core.ui.screens.habits.list.HabitCardListCache
import io.github.crisacm.salem.core.ui.screens.habits.list.HintList
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsMenuBehavior
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.utils.MidnightTimer
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.ui.BaseUiTest
import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals

/** The whole screen of the list over a model of real state holders and no habits. */
@OptIn(ExperimentalTestApi::class)
class ListHabitsContentTest : BaseUiTest() {
    private class Model(
        override val menu: ListHabitsMenu,
        override val selectionMenu: ListHabitsSelectionMenu,
        override val listState: HabitListState,
        override val preferences: Preferences
    ) : ListScreenModel {
        var attached = 0
        var detached = 0
        var width = 0
        override val headerState = HeaderState(bucketPx = 100f) {}
        override val rowCallbacks = ListRowCallbacks({}, {}, { _, _, _, _, _, _ -> }, { _, _, _, _ -> }, { _, _ -> }, {}, {})
        override val emptyState = mutableStateOf(EmptyListState.HIDDEN)
        override val hintList = HintList(preferences, emptyArray())
        override val taskRunner = object : TaskRunner {
            override val activeTaskCount = 0

            override fun addListener(listener: TaskRunner.Listener) {}

            override fun removeListener(listener: TaskRunner.Listener) {}

            override fun execute(task: Task) {}

            override fun publishProgress(task: Task, progress: Int) {}

            override suspend fun await() {}
        }
        override val dateFormatter = JavaLocalDateFormatter(Locale.US)
        override val colors = ListScreenColors(
            windowBackground = Color.White,
            barColor = Color.Blue,
            onBarColor = Color.White,
            headerBackground = Color.LightGray,
            headerText = Color.Gray,
            list = ListColors(Color.White, Color.LightGray, Color.LightGray, Color.Gray, Color.Black)
        )

        override fun onAttached() {
            attached++
        }

        override fun onDetached() {
            detached++
        }

        override fun onWidthChanged(width: Int) {
            this.width = width
        }
    }

    private lateinit var model: Model

    @Before
    override fun setUp() {
        super.setUp()
        val preferences = Preferences(MemoryStorage())
        val cache = HabitCardListCache(habitList, commandRunner, taskRunner, mock())
        val state = HabitListState(cache, preferences, mock<MidnightTimer>())
        val menu = ListHabitsMenu(
            preferences,
            mock<ThemeSwitcher>(),
            ListHabitsMenuBehavior(mock(), mock(), preferences, mock<ThemeSwitcher>())
        )
        val selection = ListHabitsSelectionMenu(
            state,
            commandRunner,
            preferences,
            ListHabitsSelectionMenuBehavior(habitList, mock(), mock(), commandRunner),
            lazy { error("not used") },
            mock()
        )
        model = Model(menu, selection, state, preferences)
    }

    @Test
    fun showsTheBarTheHeaderAndTheEmptyStateAndTellsTheModelAboutItself() = runComposeUiTest {
        model.emptyState.value = EmptyListState.EMPTY
        setContent { MaterialTheme { ListHabitsContent(model, LightTheme()) } }
        onNodeWithTag("habitListScreen").assertIsDisplayed()
        onNodeWithTag("actionCreateHabit").assertIsDisplayed()
        onNodeWithTag("dayHeader").assertIsDisplayed()
        onNodeWithText("You have no active habits").assertIsDisplayed()
        assertEquals(1, model.attached)
        assertEquals(true, model.width > 0)
    }

    @Test
    fun leavingTheCompositionDetachesTheModel() = runComposeUiTest {
        val shown = mutableStateOf(true)
        setContent { if (shown.value) MaterialTheme { ListHabitsContent(model, LightTheme()) } }
        runOnIdle { shown.value = false }
        runOnIdle { assertEquals(1, model.detached) }
    }
}
