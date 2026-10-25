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
package io.github.crisacm.salem.acceptance

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import io.github.crisacm.salem.BaseUserInterfaceTest
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickComposeText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.launchApp
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.MenuItem.ABOUT
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.MenuItem.SETTINGS
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.clickMenu
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AboutTest : BaseUserInterfaceTest() {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private fun verifyDisplaysComposeText(text: String) {
        composeRule.onAllNodesWithText(text).onFirst().performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shouldDisplayAboutScreen() {
        launchApp()
        clickMenu(ABOUT)
        verifyDisplaysComposeText("Salem")
        verifyDisplaysComposeText("Rate this app on Google Play")
        verifyDisplaysComposeText("Developers")
        verifyDisplaysComposeText("Translators")
    }

    @Test
    fun shouldDisplayAboutScreenFromSettings() {
        launchApp()
        clickMenu(SETTINGS)
        clickComposeText("About")
        verifyDisplaysComposeText("Translators")
    }
}
