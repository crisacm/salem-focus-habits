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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import io.github.crisacm.salem.BaseUserInterfaceTest
import io.github.crisacm.salem.acceptance.steps.CommonSteps.Screen.EDIT_HABIT
import io.github.crisacm.salem.acceptance.steps.CommonSteps.Screen.LIST_HABITS
import io.github.crisacm.salem.acceptance.steps.CommonSteps.Screen.SELECT_HABIT_TYPE
import io.github.crisacm.salem.acceptance.steps.CommonSteps.Screen.SHOW_HABIT
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickComposeText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.launchApp
import io.github.crisacm.salem.acceptance.steps.CommonSteps.longClickText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.pressBack
import io.github.crisacm.salem.acceptance.steps.CommonSteps.verifyDisplayGraphs
import io.github.crisacm.salem.acceptance.steps.CommonSteps.verifyDisplaysText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.verifyDisplaysTextInSequence
import io.github.crisacm.salem.acceptance.steps.CommonSteps.verifyDoesNotDisplayText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.verifySelectionCount
import io.github.crisacm.salem.acceptance.steps.CommonSteps.verifyShowsScreen
import io.github.crisacm.salem.acceptance.steps.EditHabitSteps.clickSave
import io.github.crisacm.salem.acceptance.steps.EditHabitSteps.pickColor
import io.github.crisacm.salem.acceptance.steps.EditHabitSteps.pickFrequency
import io.github.crisacm.salem.acceptance.steps.EditHabitSteps.typeDescription
import io.github.crisacm.salem.acceptance.steps.EditHabitSteps.typeName
import io.github.crisacm.salem.acceptance.steps.EditHabitSteps.typeQuestion
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.MenuItem.ADD
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.MenuItem.ARCHIVE
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.MenuItem.DELETE
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.MenuItem.EDIT
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.MenuItem.TOGGLE_ARCHIVED
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.MenuItem.TOGGLE_COMPLETED
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.MenuItem.UNARCHIVE
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.clickMenu
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.longPressCheckmarks
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HabitsTest : BaseUserInterfaceTest() {

    @Test
    @Throws(Exception::class)
    fun shouldCreateHabit() {
        shouldCreateHabit("this is a test description")
    }

    @Test
    @Throws(Exception::class)
    fun shouldCreateHabitBlankDescription() {
        shouldCreateHabit("")
    }

    @Throws(Exception::class)
    private fun shouldCreateHabit(description: String) {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickMenu(ADD)
        verifyShowsScreen(SELECT_HABIT_TYPE)
        clickComposeText("Yes or No")
        verifyShowsScreen(EDIT_HABIT)
        val testName = "Hello world"
        typeName(testName)
        typeQuestion("Did you say hello to the world today?")
        typeDescription(description)
        pickFrequency()
        pickColor(5)
        clickSave()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText(testName)
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowHabitStatistics() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickText("Track time")
        verifyShowsScreen(SHOW_HABIT)
        verifyDisplayGraphs()
    }

    @Test
    @Throws(Exception::class)
    fun shouldDeleteHabit() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickText("Track time")
        clickMenu(DELETE)
        clickComposeText("Yes")
        verifyDoesNotDisplayText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldEditHabit() {
        shouldEditHabit("this is a test description")
    }

    @Test
    @Throws(Exception::class)
    fun shouldEditHabitBlankDescription() {
        shouldEditHabit("")
    }

    @Throws(Exception::class)
    private fun shouldEditHabit(description: String) {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickText("Track time")
        clickMenu(EDIT)
        verifyShowsScreen(EDIT_HABIT)
        typeName("Take a walk")
        typeQuestion("Did you take a walk today?")
        typeDescription(description)
        clickSave()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysTextInSequence("Wake up early", "Take a walk", "Meditate")
        verifyDoesNotDisplayText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldEditHabit_fromStatisticsScreen() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickText("Track time")
        verifyShowsScreen(SHOW_HABIT)
        clickMenu(EDIT)
        verifyShowsScreen(EDIT_HABIT)
        typeName("Take a walk")
        typeQuestion("Did you take a walk today?")
        pickColor(10)
        clickSave()
        verifyShowsScreen(SHOW_HABIT)
        verifyDisplaysText("Take a walk")
        pressBack()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText("Take a walk")
        verifyDoesNotDisplayText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldArchiveAndUnarchiveHabits() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickText("Track time")
        clickMenu(ARCHIVE)
        verifyDoesNotDisplayText("Track time")
        clickMenu(TOGGLE_ARCHIVED)
        verifyDisplaysText("Track time")
        longClickText("Track time")
        clickMenu(UNARCHIVE)
        clickMenu(TOGGLE_ARCHIVED)
        verifyDisplaysText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldToggleCheckmarksAndUpdateScore() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longPressCheckmarks("Wake up early", count = 2)
        clickText("Wake up early")
        verifyShowsScreen(SHOW_HABIT)
        // TODO: find a better way than sleeping in tests
        Thread.sleep(2001L)
        verifyDisplaysText("10%")
    }

    @Test
    @Throws(Exception::class)
    fun shouldHideCompleted() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText("Track time")
        verifyDisplaysText("Wake up early")
        clickMenu(TOGGLE_COMPLETED)
        verifyDoesNotDisplayText("Track time")
        verifyDisplaysText("Wake up early")
        longPressCheckmarks("Wake up early", count = 1)
        // TODO: find a better way than sleeping in tests
        Thread.sleep(2001L)
        verifyDoesNotDisplayText("Wake up early")
        clickMenu(TOGGLE_COMPLETED)
        verifyDisplaysText("Track time")
        verifyDisplaysText("Wake up early")
    }

    @Test
    @Throws(Exception::class)
    fun shouldAllowMultipleSelection() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText("Track time")
        longClickText("Wake up early")
        longClickText("Track time")
        verifySelectionCount(2)
    }
}
