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

package io.github.crisacm.salem.ui.settings

import androidx.compose.material3.Text
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
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.platform.time.DayOfWeek
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
class SettingsContentTest {
    private val preferences = Preferences(MemoryStorage())
    private val dispatcher = NavigationEventDispatcher()
    private val input = DirectNavigationEventInput()
    private val calls = mutableListOf<String>()
    private var changes = 0
    private val names = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    private fun androidx.compose.ui.test.ComposeUiTest.showSettings(
        animations: Boolean = true,
        extras: Boolean = false
    ) {
        dispatcher.addInput(input)
        val state = SettingsState(preferences) { changes++ }
        setContent {
            val owner = object : NavigationEventDispatcherOwner {
                override val navigationEventDispatcher = dispatcher
            }
            CompositionLocalProvider(LocalCompatNavigationEventDispatcherOwner provides owner) {
                SettingsContent(
                    state = state,
                    weekdayNames = names,
                    showAnimationsSwitch = animations,
                    onBack = { calls += "back" },
                    onHelp = { calls += "help" },
                    onRate = { calls += "rate" },
                    onAbout = { calls += "about" },
                    interfaceExtras = { if (extras) Text("Extra interface row") },
                    middleSections = { if (extras) Text("Extra middle section") }
                )
            }
        }
    }

    @Test
    fun aSwitchWritesThePreferenceAndTellsIt() = runComposeUiTest {
        showSettings()
        assertFalse(preferences.isSkipEnabled)
        onNodeWithText("Enable skip days").performClick()
        assertTrue(preferences.isSkipEnabled)
        assertEquals(1, changes)
    }

    @Test
    fun theFirstDayOfTheWeekIsChosenInADialog() = runComposeUiTest {
        showSettings()
        onNodeWithText("First day of the week").performScrollTo().performClick()
        onNodeWithText("Monday").performClick()
        assertEquals(DayOfWeek.MONDAY, preferences.firstWeekday)
        onNodeWithText("Monday").assertIsDisplayed()
    }

    @Test
    fun theAnimationsRowCanBeHidden() = runComposeUiTest {
        showSettings(animations = false)
        onNodeWithText("Enable skip days").assertIsDisplayed()
        onNodeWithText("Disable animations").assertDoesNotExist()
    }

    @Test
    fun theAnimationsRowIsShownByDefault() = runComposeUiTest {
        showSettings()
        onNodeWithText("Disable animations").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun theDeveloperSwitchIsHiddenUntilTheDeveloperModeIsOn() = runComposeUiTest {
        showSettings()
        onNodeWithText("Enable developer mode").assertDoesNotExist()
    }

    @Test
    fun turningTheDeveloperModeOffHidesItsSection() = runComposeUiTest {
        preferences.isDeveloper = true
        showSettings()
        onNodeWithText("Development").performScrollTo().assertIsDisplayed()
        onNodeWithText("Enable developer mode").performScrollTo().performClick()
        assertFalse(preferences.isDeveloper)
        onNodeWithText("Enable developer mode").assertDoesNotExist()
        onNodeWithText("Development").assertDoesNotExist()
    }

    @Test
    fun theBackButtonGoesBack() = runComposeUiTest {
        showSettings()
        onNodeWithTag("settingsBack").performClick()
        assertEquals(listOf("back"), calls)
    }

    @Test
    fun everySectionOfTheSharedScreenIsShown() = runComposeUiTest {
        showSettings()
        onNodeWithText("Interface").assertIsDisplayed()
        onNodeWithText("Links").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun theLinksCallTheirActions() = runComposeUiTest {
        showSettings()
        onNodeWithText("Help & FAQ").performScrollTo().performClick()
        onNodeWithText("Rate this app on Google Play").performScrollTo().performClick()
        onNodeWithText("About").performScrollTo().performClick()
        assertEquals(listOf("help", "rate", "about"), calls)
    }

    @Test
    fun theHostAddsItsOwnRows() = runComposeUiTest {
        showSettings(extras = true)
        onNodeWithText("Extra interface row").performScrollTo().assertIsDisplayed()
        onNodeWithText("Extra middle section").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun theBackButtonAndTheBackGestureLeave() = runComposeUiTest {
        showSettings()
        onNodeWithContentDescription("Navigate up").performClick()
        runOnIdle {
            input.backStarted(NavigationEvent(NavigationEvent.EDGE_NONE, 0f, 0f, 0f, 0L))
            input.backCompleted()
        }
        assertEquals(listOf("back", "back"), calls)
    }
}
