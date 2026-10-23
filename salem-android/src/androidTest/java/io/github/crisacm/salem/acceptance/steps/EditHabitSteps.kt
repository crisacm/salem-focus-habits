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
import io.github.crisacm.salem.BaseUserInterfaceTest
import io.github.crisacm.salem.R
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickComposeText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickComposeTextIgnoreCase
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickLastComposeText

object EditHabitSteps {
    fun clickSave() {
        Espresso.onView(ViewMatchers.withId(R.id.buttonSave)).perform(ViewActions.click())
    }

    // The frequency dialog is drawn by Compose, which Espresso view matchers cannot see.
    fun pickFrequency() {
        Espresso.onView(ViewMatchers.withId(R.id.boolean_frequency_picker))
            .perform(ViewActions.click())
        clickComposeTextIgnoreCase("Save")
    }

    fun pickMonthFrequency() {
        Espresso.onView(ViewMatchers.withId(R.id.boolean_frequency_picker))
            .perform(ViewActions.click())
        clickComposeText(textAfterNumber(R.string.x_times_per_month))
        val field = BaseUserInterfaceTest.device.findObject(By.clazz("android.widget.EditText").text("10"))
        field.text = "1"
        clickComposeTextIgnoreCase("Save")
    }

    fun pickDailyFrequency() {
        Espresso.onView(ViewMatchers.withId(R.id.boolean_frequency_picker))
            .perform(ViewActions.click())
        clickLastComposeText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.every_day))
        clickComposeTextIgnoreCase("Save")
    }

    /** The text of a "%d times per month" style string that follows the number. */
    private fun textAfterNumber(id: Int) =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id).substringAfter("%d").trim()

    fun pickColor(color: Int) {
        Espresso.onView(ViewMatchers.withId(R.id.colorButton)).perform(ViewActions.click())
        BaseUserInterfaceTest.device.findObject(By.descStartsWith(String.format("Color %d", color)))
            .click()
    }

    fun typeName(name: String) {
        typeTextWithId(R.id.nameInput, name)
    }

    fun typeQuestion(name: String) {
        typeTextWithId(R.id.questionInput, name)
    }

    fun typeDescription(description: String) {
        typeTextWithId(R.id.notesInput, description)
    }

    fun setReminder() {
        Espresso.onView(ViewMatchers.withId(R.id.reminderTimePicker)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.done_button)).perform(ViewActions.click())
    }

    fun clickReminderDays() {
        Espresso.onView(ViewMatchers.withId(R.id.reminderDatePicker)).perform(ViewActions.click())
    }

    fun unselectAllDays() {
        clickComposeText("Saturday")
        clickComposeText("Sunday")
        clickComposeText("Monday")
        clickComposeText("Tuesday")
        clickComposeText("Wednesday")
        clickComposeText("Thursday")
        clickComposeText("Friday")
    }

    private fun typeTextWithId(id: Int, name: String) {
        Espresso.onView(ViewMatchers.withId(id)).perform(
            ViewActions.clearText(),
            ViewActions.typeText(name),
            ViewActions.closeSoftKeyboard()
        )
    }
}
