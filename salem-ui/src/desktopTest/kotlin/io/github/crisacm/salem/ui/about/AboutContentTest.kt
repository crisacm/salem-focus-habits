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

package io.github.crisacm.salem.ui.about

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class)
class AboutContentTest {
    private val preferences = Preferences(MemoryStorage())
    private val opened = mutableListOf<String>()
    private var backs = 0

    private fun androidx.compose.ui.test.ComposeUiTest.showAbout() = setContent {
        AboutRoute("1.2.3", preferences, { opened += it }, { backs++ })
    }

    @Test
    fun showsTheVersionTheLinksAndTheCredits() = runComposeUiTest {
        showAbout()
        onNodeWithText("Version 1.2.3").assertIsDisplayed()
        onNodeWithText("Rate this app on Google Play").assertIsDisplayed()
        onNodeWithText("Developers").performScrollTo().assertIsDisplayed()
        onNodeWithText("Translators").performScrollTo().assertIsDisplayed()
        onNodeWithText("Português").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun linksOpenTheirPages() = runComposeUiTest {
        showAbout()
        onNodeWithText("Rate this app on Google Play").performClick()
        onNodeWithText("View all contributors…").performScrollTo().performClick()
        assertEquals(listOf(SalemLinks.RATE, SalemLinks.CONTRIBUTORS), opened)
    }

    @Test
    fun fiveTapsOnTheVersionTurnOnTheDeveloperMode() = runComposeUiTest {
        showAbout()
        repeat(4) { onNodeWithText("Version 1.2.3").performClick() }
        assertFalse(preferences.isDeveloper)
        onNodeWithText("Version 1.2.3").performClick()
        assertTrue(preferences.isDeveloper)
        onNodeWithText("You are now a developer").assertIsDisplayed()
    }

    @Test
    fun theBackButtonLeaves() = runComposeUiTest {
        showAbout()
        onNodeWithContentDescription("Navigate up").performClick()
        assertEquals(1, backs)
    }
}
