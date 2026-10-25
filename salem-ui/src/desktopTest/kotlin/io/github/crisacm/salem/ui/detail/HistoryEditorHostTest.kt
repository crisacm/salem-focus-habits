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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardState
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.BaseUiTest
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
class HistoryEditorHostTest : BaseUiTest() {
    private val layer = HistoryEditorLayer()
    private val presses = mutableListOf<LocalDate>()
    private val listener = object : OnDateClickedListener {
        override fun onDateShortPress(date: LocalDate) {
            presses += date
        }

        override fun onDateLongPress(date: LocalDate) {}
    }
    private val dispatcher = NavigationEventDispatcher()
    private val input = DirectNavigationEventInput().also { dispatcher.addInput(it) }

    private fun stateOfAHabit(): HistoryCardState {
        val habit = fixtures.createShortHabit()
        habitList.add(habit)
        return HistoryCardPresenter.buildState(habit, DayOfWeek.SUNDAY, LightTheme())
    }

    private fun androidx.compose.ui.test.ComposeUiTest.show(state: HistoryCardState?) = setContent {
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = dispatcher
        }
        CompositionLocalProvider(LocalCompatNavigationEventDispatcherOwner provides owner) {
            MaterialTheme { HistoryEditorHost(layer, state, JavaLocalDateFormatter(Locale.US)) }
        }
    }

    private fun sendBack() {
        input.backStarted(NavigationEvent(NavigationEvent.EDGE_NONE, 0f, 0f, 0f, 0L))
        input.backCompleted()
    }

    @Test
    fun nothingIsShownWhileTheLayerIsClosed() = runComposeUiTest {
        show(stateOfAHabit())
        onNodeWithTag("historyEditorChart").assertDoesNotExist()
    }

    @Test
    fun nothingIsShownWithoutAState() = runComposeUiTest {
        show(null)
        runOnIdle { layer.show(listener) }
        onNodeWithTag("historyEditorChart").assertDoesNotExist()
    }

    @Test
    fun theCalendarIsShownWhenTheLayerOpens() = runComposeUiTest {
        show(stateOfAHabit())
        runOnIdle { layer.show(listener) }
        onNodeWithTag("historyEditorChart").assertIsDisplayed()
    }

    @Test
    fun aTapOnTheCalendarReachesTheListenerAndKeepsItOpen() = runComposeUiTest {
        show(stateOfAHabit())
        runOnIdle { layer.show(listener) }
        onNodeWithTag("historyEditorChart").performTouchInput { click(center) }
        waitForIdle()
        assertEquals(1, presses.size)
        assertTrue(layer.isOpen)
    }

    @Test
    fun aTapOutsideTheCalendarClosesIt() = runComposeUiTest {
        show(stateOfAHabit())
        runOnIdle { layer.show(listener) }
        onRoot().performTouchInput { click(Offset(1f, 1f)) }
        waitForIdle()
        assertFalse(layer.isOpen)
        assertTrue(presses.isEmpty())
    }

    @Test
    fun backClosesIt() = runComposeUiTest {
        show(stateOfAHabit())
        runOnIdle { layer.show(listener) }
        runOnIdle { sendBack() }
        runOnIdle { assertFalse(layer.isOpen) }
    }

    @Test
    fun aNewStateKeepsTheCalendarOpen() = runComposeUiTest {
        val first = stateOfAHabit()
        var state by mutableStateOf<HistoryCardState?>(first)
        setContent { MaterialTheme { HistoryEditorHost(layer, state, JavaLocalDateFormatter(Locale.US)) } }
        runOnIdle { layer.show(listener) }
        onNodeWithTag("historyEditorChart").assertIsDisplayed()
        runOnIdle { state = first.copy(notesIndicators = first.notesIndicators.map { !it }) }
        onNodeWithTag("historyEditorChart").assertIsDisplayed()
        assertTrue(layer.isOpen)
    }
}
