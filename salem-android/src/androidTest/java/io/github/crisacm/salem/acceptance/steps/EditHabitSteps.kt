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

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import io.github.crisacm.salem.BaseUserInterfaceTest
import io.github.crisacm.salem.R
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickComposeText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickComposeTextIgnoreCase
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickLastComposeText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickTag
import io.github.crisacm.salem.acceptance.steps.CommonSteps.setTagText

object EditHabitSteps {
    fun clickSave() {
        clickTag("buttonSave")
    }

    // The frequency dialog is drawn by Compose, which Espresso view matchers cannot see.
    fun pickFrequency() {
        clickTag("frequencyPicker")
        clickComposeTextIgnoreCase("Save")
    }

    fun pickMonthFrequency() {
        clickTag("frequencyPicker")
        clickComposeText(textAfterNumber(R.string.x_times_per_month))
        val field = BaseUserInterfaceTest.device.findObject(By.clazz("android.widget.EditText").text("10"))
        field.text = "1"
        clickComposeTextIgnoreCase("Save")
    }

    fun pickDailyFrequency() {
        clickTag("frequencyPicker")
        clickLastComposeText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.every_day))
        clickComposeTextIgnoreCase("Save")
    }

    /** The text of a "%d times per month" style string that follows the number. */
    private fun textAfterNumber(id: Int) =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id).substringAfter("%d").trim()

    fun pickColor(color: Int) {
        clickTag("colorButton")
        BaseUserInterfaceTest.device.findObject(By.descStartsWith(String.format("Color %d", color)))
            .click()
    }

    fun typeName(name: String) {
        setTagText("nameInput", name)
    }

    fun typeQuestion(name: String) {
        setTagText("questionInput", name)
    }

    fun typeDescription(description: String) {
        setTagText("notesInput", description)
    }

    fun setReminder() {
        clickTag("reminderTimePicker")
        clickTag("timePickerOk")
    }
}
