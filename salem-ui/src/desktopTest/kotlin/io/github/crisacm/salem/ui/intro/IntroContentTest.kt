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

package io.github.crisacm.salem.ui.intro

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.backhandler.LocalCompatNavigationEventDispatcherOwner
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
class IntroContentTest {
    private val dispatcher = NavigationEventDispatcher()
    private val input = DirectNavigationEventInput()
    private var finished = 0

    private fun androidx.compose.ui.test.ComposeUiTest.showIntro() {
        dispatcher.addInput(input)
        setContent {
            val owner = object : NavigationEventDispatcherOwner {
                override val navigationEventDispatcher = dispatcher
            }
            CompositionLocalProvider(LocalCompatNavigationEventDispatcherOwner provides owner) {
                IntroContent(onFinish = { finished++ })
            }
        }
    }

    private fun sendBack() {
        input.backStarted(NavigationEvent(NavigationEvent.EDGE_NONE, 0f, 0f, 0f, 0L))
        input.backCompleted()
    }

    @Test
    fun startsOnTheFirstPageWithSkipAndNext() = runComposeUiTest {
        showIntro()
        onNodeWithText("Welcome").assertIsDisplayed()
        onNodeWithText("Skip").assertIsDisplayed()
        onNodeWithText("Next").assertIsDisplayed()
        onNodeWithText("Done").assertDoesNotExist()
    }

    @Test
    fun nextReachesTheLastPageWhichHasDone() = runComposeUiTest {
        showIntro()
        onNodeWithText("Next").performClick()
        waitForIdle()
        onNodeWithText("Next").performClick()
        waitForIdle()
        onNodeWithText("Done").assertIsDisplayed()
        onNodeWithText("Skip").assertDoesNotExist()
        onNodeWithText("Next").assertDoesNotExist()
    }

    @Test
    fun skipAndDoneFinish() = runComposeUiTest {
        showIntro()
        onNodeWithText("Skip").performClick()
        assertEquals(1, finished)
        onNodeWithText("Next").performClick()
        waitForIdle()
        onNodeWithText("Next").performClick()
        waitForIdle()
        onNodeWithText("Done").performClick()
        assertEquals(2, finished)
    }

    @Test
    fun backGoesToThePreviousPageAndIsLeftAloneOnTheFirst() = runComposeUiTest {
        showIntro()
        onNodeWithText("Next").performClick()
        waitForIdle()
        onNodeWithText("Skip").assertIsDisplayed()
        runOnIdle { sendBack() }
        waitForIdle()
        onNodeWithText("Welcome").assertIsDisplayed()
        // On the first page the handler is off, so back does not finish the intro
        runOnIdle { sendBack() }
        waitForIdle()
        assertEquals(0, finished)
    }
}
