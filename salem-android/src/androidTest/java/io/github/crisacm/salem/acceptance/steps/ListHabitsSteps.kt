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
package io.github.crisacm.salem.acceptance.steps

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import io.github.crisacm.salem.BaseUserInterfaceTest.Companion.device
import io.github.crisacm.salem.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.endsWith
import java.util.regex.Pattern

object ListHabitsSteps {
    // The top bar and the selection bar of the list are drawn by Compose, which Espresso view matchers
    // cannot see: their buttons are found by test tag. The screen of a habit still has an action bar
    // made of Views, so the steps that both screens share look for the Compose button first.
    fun clickMenu(item: MenuItem?) {
        when (item) {
            MenuItem.ABOUT -> clickInOverflow("overflowMenu", R.string.about)
            MenuItem.HELP -> clickInOverflow("overflowMenu", R.string.help)
            MenuItem.SETTINGS -> clickInOverflow("overflowMenu", R.string.settings)
            MenuItem.ADD -> CommonSteps.clickTag("actionCreateHabit")
            MenuItem.EDIT -> {
                if (hasComposeButton("action_edit_habit")) {
                    CommonSteps.clickTag("action_edit_habit")
                } else {
                    clickViewWithId(R.id.action_edit_habit)
                }
            }
            MenuItem.DELETE -> clickInOverflow("selectionOverflow", R.string.delete)
            MenuItem.ARCHIVE -> clickInOverflow("selectionOverflow", R.string.archive)
            MenuItem.UNARCHIVE -> clickInOverflow("selectionOverflow", R.string.unarchive)
            MenuItem.TOGGLE_ARCHIVED -> {
                CommonSteps.clickTag("action_filter")
                CommonSteps.clickComposeText(string(R.string.hide_archived))
            }
            MenuItem.TOGGLE_COMPLETED -> {
                CommonSteps.clickTag("action_filter")
                CommonSteps.clickComposeText(string(R.string.hide_completed))
            }
            else -> throw RuntimeException()
        }
        device.waitForIdle()
    }

    // Compose needs a moment to show the selection bar after a long press, so wait before falling back to the Views.
    private fun hasComposeButton(tag: String) = device.wait(Until.hasObject(By.res(tag)), 1500)

    private fun string(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun clickInOverflow(tag: String, id: Int) {
        if (hasComposeButton(tag)) {
            CommonSteps.clickTag(tag)
            CommonSteps.clickComposeText(string(id))
        } else {
            clickTextInsideOverflowMenu(id)
        }
    }

    private fun clickTextInsideOverflowMenu(id: Int) {
        Espresso.onView(
            allOf(
                ViewMatchers.withContentDescription("More options"),
                ViewMatchers.withParent(
                    ViewMatchers.withParent(
                        ViewMatchers.withClassName(
                            endsWith("Toolbar")
                        )
                    )
                )
            )
        ).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(id)).perform(ViewActions.click())
    }

    private fun clickViewWithId(id: Int) {
        Espresso.onView(ViewMatchers.withId(id)).perform(ViewActions.click())
    }

    /**
     * The checkmark cells of the row of [habitName], from left to right. The rows and cells are drawn by
     * Compose: a cell is found by its test tag (`checkmark_<value>`) and belongs to the row whose vertical
     * span contains the name of the habit.
     */
    fun checkmarkCells(habitName: String): List<UiObject2> {
        val name = device.findObject(By.text(habitName)) ?: throw AssertionError("Habit not shown: $habitName")
        val y = name.visibleBounds.centerY()
        val row = device.findObjects(By.res("habitRow")).map { it.visibleBounds }.firstOrNull { y in it.top..it.bottom }
            ?: throw AssertionError("Row of $habitName not found")
        return device.findObjects(By.res(CHECKMARK_CELL))
            .filter { it.visibleBounds.centerY() in row.top..row.bottom }
            .sortedBy { it.visibleBounds.left }
    }

    /** The values of the checkmark cells of the row of [habitName], from today backwards. */
    fun checkmarkValues(habitName: String): List<Int> =
        checkmarkCells(habitName).map { it.resourceName.substringAfter("checkmark_").toInt() }

    /** Long presses the [count] first checkmarks (the most recent days) of a habit. */
    fun longPressCheckmarks(habit: String, count: Int) {
        CommonSteps.scrollToText(habit)
        // The cells are found again each time because a long press recomposes the row.
        for (i in 0 until count) {
            checkmarkCells(habit)[i].longClick()
            device.waitForIdle()
        }
    }

    private val CHECKMARK_CELL = Pattern.compile("checkmark_.*")

    fun changeSort(sortText: String) {
        CommonSteps.clickTag("action_filter")
        CommonSteps.clickComposeText("Sort")
        CommonSteps.clickComposeText(sortText)
    }

    enum class MenuItem {
        ABOUT, HELP, SETTINGS, EDIT, DELETE, ARCHIVE, TOGGLE_ARCHIVED, UNARCHIVE, TOGGLE_COMPLETED, ADD
    }
}
