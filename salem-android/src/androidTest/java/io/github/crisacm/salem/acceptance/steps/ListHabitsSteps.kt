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

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import io.github.crisacm.salem.BaseUserInterfaceTest
import io.github.crisacm.salem.BaseUserInterfaceTest.Companion.device
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.habits.list.views.CheckmarkButtonView
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.Matcher
import java.util.LinkedList

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

    private fun longClickDescendantWithClass(cls: Class<*>, count: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isEnabled()
            }

            override fun getDescription(): String {
                return "perform on children"
            }

            override fun perform(uiController: UiController, view: View) {
                val stack = LinkedList<ViewGroup>()
                if (view is ViewGroup) stack.push(view)
                var countRemaining = count
                while (!stack.isEmpty()) {
                    val vg = stack.pop()
                    for (i in 0 until vg.childCount) {
                        val v = vg.getChildAt(i)
                        if (v is ViewGroup) stack.push(v)
                        if (cls.isInstance(v) && countRemaining > 0) {
                            v.performLongClick()
                            uiController.loopMainThreadUntilIdle()
                            countRemaining--
                        }
                    }
                }
            }
        }
    }

    fun longPressCheckmarks(habit: String?, count: Int) {
        CommonSteps.scrollToText(habit)
        Espresso.onView(
            allOf(
                ViewMatchers.hasDescendant(ViewMatchers.withText(habit)),
                ViewMatchers.withClassName(endsWith("HabitCardView"))
            )
        ).perform(
            longClickDescendantWithClass(CheckmarkButtonView::class.java, count)
        )
        BaseUserInterfaceTest.device.waitForIdle()
    }

    fun changeSort(sortText: String) {
        CommonSteps.clickTag("action_filter")
        CommonSteps.clickComposeText("Sort")
        CommonSteps.clickComposeText(sortText)
    }

    enum class MenuItem {
        ABOUT, HELP, SETTINGS, EDIT, DELETE, ARCHIVE, TOGGLE_ARCHIVED, UNARCHIVE, TOGGLE_COMPLETED, ADD
    }
}
