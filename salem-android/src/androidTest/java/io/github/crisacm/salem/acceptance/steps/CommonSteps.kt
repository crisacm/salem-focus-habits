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

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.PositionAssertions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import io.github.crisacm.salem.BaseUserInterfaceTest
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.habits.list.ListHabitsActivity
import org.hamcrest.CoreMatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.util.regex.Pattern

object CommonSteps : BaseUserInterfaceTest() {
    fun pressBack() {
        device.pressBack()
    }

    // The rows of the habit list are drawn by Compose, which Espresso view matchers cannot see, so while
    // the list is on screen the helpers that take a text look for it with UiAutomator.
    private fun isHabitListShown() = device.hasObject(By.res("habitRow"))

    private fun scrollListTo(text: String) {
        val scrollable = UiScrollable(UiSelector().scrollable(true))
        try {
            if (scrollable.exists()) scrollable.scrollIntoView(UiSelector().text(text))
        } catch (e: UiObjectNotFoundException) {
            // The assertion that follows reports the missing text.
        }
    }

    private fun findListText(text: String): UiObject2 {
        scrollListTo(text)
        assertTrue("Text not found: $text", device.wait(Until.hasObject(By.text(text)), 5000))
        return device.findObject(By.text(text))
    }

    fun clickText(text: String?) {
        if (text != null && isHabitListShown()) {
            findListText(text).click()
            device.waitForIdle()
            return
        }
        scrollToText(text)
        Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.click())
        device.waitForIdle()
    }

    /** Clicks text drawn by Compose, which Espresso view matchers cannot see. */
    fun clickComposeText(text: String) {
        val scrollable = UiScrollable(UiSelector().scrollable(true))
        if (scrollable.exists()) scrollable.scrollIntoView(UiSelector().text(text))
        assertTrue("Text not found: $text", device.wait(Until.hasObject(By.text(text)), 5000))
        device.findObject(UiSelector().text(text)).click()
        device.waitForIdle()
    }

    /**
     * Clicks the last match of [text]. A dialog is drawn above its screen, which may show the same text
     * (the "Every day" label of the frequency button), and the topmost match is the one wanted.
     */
    fun clickLastComposeText(text: String) {
        assertTrue("Text not found: $text", device.wait(Until.hasObject(By.text(text)), 5000))
        device.findObjects(By.text(text)).last().click()
        device.waitForIdle()
    }

    /** Clicks the Compose element with the given `testTag` (exposed as a resource id). */
    fun clickTag(tag: String) {
        assertTrue("Tag not found: $tag", device.wait(Until.hasObject(By.res(tag)), 5000))
        device.findObject(By.res(tag)).click()
        device.waitForIdle()
    }

    /** Waits until the selection bar of the habit list shows [count] selected habits. */
    fun verifySelectionCount(count: Int) {
        assertTrue(
            "Selection count $count not shown",
            device.wait(Until.hasObject(By.res("selectionCount").text(count.toString())), 5000)
        )
    }

    /** Replaces the text of the Compose field with the given `testTag`. */
    fun setTagText(tag: String, text: String) {
        assertTrue("Tag not found: $tag", device.wait(Until.hasObject(By.res(tag)), 5000))
        device.findObject(By.res(tag)).text = text
        device.waitForIdle()
    }

    /** Like [clickComposeText], for buttons whose label case may change (Compose does not capitalize them). */
    fun clickComposeTextIgnoreCase(text: String) {
        val pattern = Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE)
        assertTrue("Text not found: $text", device.wait(Until.hasObject(By.text(pattern)), 5000))
        device.findObject(By.text(pattern)).click()
        device.waitForIdle()
    }

    fun clickText(@StringRes id: Int) {
        Espresso.onView(ViewMatchers.withText(id)).perform(ViewActions.click())
    }

    fun launchApp() {
        startActivity(ListHabitsActivity::class.java)
        assertTrue(
            device.wait(Until.hasObject(By.pkg("io.github.crisacm.salem")), 5000)
        )
        device.waitForIdle()
    }

    fun longClickText(text: String?) {
        if (text != null && isHabitListShown()) {
            findListText(text).longClick()
            device.waitForIdle()
            return
        }
        scrollToText(text)
        Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.longClick())
        device.waitForIdle()
    }

    fun pressHome() {
        device.pressHome()
        device.waitForIdle()
    }

    /** Drags the day header (drawn by Compose, found by its test tag) to the left to show older days. */
    fun offsetHeaders() {
        assertTrue("Day header not found", device.wait(Until.hasObject(By.res("dayHeader")), 5000))
        val bounds = device.findObject(By.res("dayHeader")).visibleBounds
        val y = bounds.centerY()
        device.swipe(bounds.right - bounds.width() / 4, y, bounds.right - bounds.width() / 2, y, 20)
    }

    fun scrollToText(text: String?) {
        if (text != null && isHabitListShown()) {
            scrollListTo(text)
            return
        }
        try {
            if (device
                .findObject(UiSelector().className(RecyclerView::class.java))
                .exists()
            ) {
                Espresso.onView(CoreMatchers.instanceOf(RecyclerView::class.java)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        ViewMatchers.hasDescendant(ViewMatchers.withText(text))
                    )
                )
            } else {
                Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.scrollTo())
            }
        } catch (e: PerformException) {
            // ignored
        }
    }

    fun verifyDisplayGraphs() {
        // The cards are Compose; their charts are found by test tag.
        for (tag in listOf("historyChart", "scoreChart")) {
            assertTrue("Chart not shown: $tag", device.wait(Until.hasObject(By.res(tag)), 5000))
        }
    }

    fun verifyDisplaysText(text: String?) {
        if (text != null && isHabitListShown()) {
            findListText(text)
            return
        }
        scrollToText(text)
        Espresso.onView(ViewMatchers.withText(text))
            .check(ViewAssertions.matches(ViewMatchers.isEnabled()))
    }

    fun verifyDisplaysTextInSequence(vararg text: String?) {
        if (isHabitListShown()) {
            var previous = findListText(text[0]!!).visibleBounds
            for (i in 1 until text.size) {
                val bounds = findListText(text[i]!!).visibleBounds
                assertTrue("${text[i]} is not below ${text[i - 1]}", bounds.top >= previous.bottom)
                previous = bounds
            }
            return
        }
        verifyDisplaysText(text[0])
        for (i in 1 until text.size) {
            verifyDisplaysText(text[i])
            Espresso.onView(ViewMatchers.withText(text[i])).check(
                PositionAssertions.isCompletelyBelow(
                    ViewMatchers.withText(
                        text[i - 1]
                    )
                )
            )
        }
    }

    fun verifyDoesNotDisplayText(text: String?) {
        Espresso.onView(ViewMatchers.withText(text)).check(ViewAssertions.doesNotExist())
        // Espresso does not see text drawn by Compose, so UiAutomator checks the whole screen too.
        if (text != null) assertTrue("Text still shown: $text", device.wait(Until.gone(By.text(text)), 3000))
        device.waitForIdle()
    }

    @Throws(Exception::class)
    fun verifyOpensWebsite(url: String) {
        var browserPkg = "org.chromium.webview_shell"
        if (SDK_INT <= Build.VERSION_CODES.M) {
            browserPkg = "com.android.browser"
        }
        assertTrue(device.wait(Until.hasObject(By.pkg(browserPkg)), 5000))
        device.waitForIdle()
        assertTrue(device.findObject(UiSelector().textContains(url)).exists())
    }

    fun verifyShowsScreen(screen: Screen?) {
        when (screen) {
            Screen.LIST_HABITS ->
                // The whole list screen is one Compose tree, found by the tag of its root.
                assertTrue("Habit list not shown", device.wait(Until.hasObject(By.res("habitListScreen")), 5000))

            Screen.SHOW_HABIT ->
                // The cards of the detail screen are Compose.
                assertTrue("Habit screen not shown", device.wait(Until.hasObject(By.res("subtitleCard")), 5000))

            Screen.EDIT_HABIT ->
                // The form is drawn by Compose; its fields are exposed by their test tag as resource ids.
                assertTrue(device.wait(Until.hasObject(By.res("questionInput")), 5000))

            Screen.SELECT_HABIT_TYPE -> {
                // The dialog is drawn by Compose, which Espresso view matchers cannot see.
                val example = InstrumentationRegistry.getInstrumentation().targetContext
                    .getString(R.string.yes_or_no_example)
                assertTrue(device.wait(Until.hasObject(By.text(example)), 5000))
            }

            else -> throw IllegalStateException()
        }
    }

    fun verifyDisplaysCheckmarks(habitName: String, vals: List<Int>) {
        scrollToText(habitName)
        val actual = ListHabitsSteps.checkmarkValues(habitName)
        assertEquals("Checkmarks of $habitName", vals, actual.take(vals.size))
    }

    fun createHabit(habitName: String) {
        ListHabitsSteps.clickMenu(ListHabitsSteps.MenuItem.ADD)
        verifyShowsScreen(Screen.SELECT_HABIT_TYPE)
        clickComposeText("Yes or No")
        verifyShowsScreen(Screen.EDIT_HABIT)
        EditHabitSteps.typeName(habitName)
        EditHabitSteps.clickSave()
    }

    fun changeFrequencyToDaily(habitName: String) {
        clickText(habitName)
        Espresso.onView(ViewMatchers.withId(R.id.action_edit_habit)).perform(ViewActions.click())
        EditHabitSteps.pickDailyFrequency()
        EditHabitSteps.clickSave()
        pressBack()
    }

    fun changeFrequencyToMonthly(habitName: String) {
        clickText(habitName)
        Espresso.onView(ViewMatchers.withId(R.id.action_edit_habit)).perform(ViewActions.click())
        EditHabitSteps.pickMonthFrequency()
        EditHabitSteps.clickSave()
        pressBack()
    }

    enum class Screen {
        LIST_HABITS, SHOW_HABIT, EDIT_HABIT, SELECT_HABIT_TYPE
    }
}
