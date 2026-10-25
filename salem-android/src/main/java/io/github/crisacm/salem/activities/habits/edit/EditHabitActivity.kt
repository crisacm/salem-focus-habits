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

package io.github.crisacm.salem.activities.habits.edit

import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.DialogFragment
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.AndroidThemeSwitcher
import io.github.crisacm.salem.activities.common.dialogs.ChoiceDialog
import io.github.crisacm.salem.activities.common.dialogs.ColorPickerDialogFactory
import io.github.crisacm.salem.activities.common.dialogs.FrequencyPickerDialog
import io.github.crisacm.salem.activities.common.dialogs.TimePickerDialog
import io.github.crisacm.salem.activities.common.dialogs.WeekdayPickerDialog
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm
import io.github.crisacm.salem.core.ui.screens.habits.edit.HabitEditorSaver
import io.github.crisacm.salem.core.ui.screens.habits.edit.SaveResult
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.ui.compose.SalemTheme
import io.github.crisacm.salem.ui.edit.EditHabitFormSaver
import io.github.crisacm.salem.ui.edit.denominatorForChoice
import io.github.crisacm.salem.ui.edit.reminderStartTime
import io.github.crisacm.salem.utils.dismissCurrentAndShow

/**
 * Screen that creates a habit (extra `habitType`) or edits one (extra `habitId`).
 */
class EditHabitActivity : AppCompatActivity() {
    private lateinit var themeSwitcher: AndroidThemeSwitcher
    private lateinit var saver: HabitEditorSaver
    private var habitId = -1L
    private var formState: MutableState<EditHabitForm>? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val component = (application as HabitsApplication).component
        themeSwitcher = AndroidThemeSwitcher(this, component.preferences)
        saver = HabitEditorSaver(component.modelFactory, component.habitList, component.commandRunner)
        themeSwitcher.apply()

        val initial = if (intent.hasExtra("habitId")) {
            habitId = intent.getLongExtra("habitId", -1)
            val habit = component.habitList.getById(habitId)
            if (habit == null) {
                finish()
                return
            }
            EditHabitForm.from(habit)
        } else {
            EditHabitForm.new(HabitType.fromInt(intent.getIntExtra("habitType", HabitType.YES_NO.value)))
        }

        val actions = EditHabitActions(
            onBack = ::finish,
            onSave = ::save,
            onPickColor = ::pickColor,
            onPickFrequency = ::pickFrequency,
            onPickTargetType = ::pickTargetType,
            onPickNumericalFrequency = ::pickNumericalFrequency,
            onPickTime = ::pickTime,
            onPickDays = ::pickDays
        )
        setContent {
            val form = rememberSaveable(stateSaver = EditHabitFormSaver) { mutableStateOf(initial) }
            SideEffect { formState = form }
            val theme = themeSwitcher.currentTheme
            val habitColor = Color(theme.color(form.value.color).toInt())
            SalemTheme(theme) {
                val night = themeSwitcher.isNightMode
                EditHabitContent(
                    form = form.value,
                    onFormChange = { form.value = it },
                    isEditing = habitId >= 0,
                    habitColor = habitColor,
                    barColor = if (night) MaterialTheme.colorScheme.surface else habitColor,
                    onBarColor = if (night) MaterialTheme.colorScheme.onSurface else Color.White,
                    actions = actions
                )
            }
        }

        // The dialogs do not keep their listeners, so none of them can be used after a recreation.
        if (state != null) {
            supportFragmentManager.fragments.filterIsInstance<DialogFragment>().forEach { it.dismissAllowingStateLoss() }
        }
    }

    private val form get() = formState!!.value

    private fun update(change: (EditHabitForm) -> EditHabitForm) {
        formState?.let { it.value = change(it.value) }
    }

    private fun pickColor() {
        val picker = ColorPickerDialogFactory().create(form.color)
        picker.setListener { color -> update { it.copy(color = color) } }
        picker.dismissCurrentAndShow(supportFragmentManager, "colorPicker")
    }

    private fun pickFrequency() {
        val picker = FrequencyPickerDialog(form.freqNum, form.freqDen)
        picker.onFrequencyPicked = { num, den -> update { it.copy(freqNum = num, freqDen = den) } }
        picker.dismissCurrentAndShow(supportFragmentManager, "frequencyPicker")
    }

    private fun pickTargetType() {
        ChoiceDialog.newInstance(
            listOf(getString(R.string.target_type_at_least), getString(R.string.target_type_at_most))
        ) { which ->
            val type = if (which == 0) NumericalHabitType.AT_LEAST else NumericalHabitType.AT_MOST
            update { it.copy(targetType = type) }
        }.dismissCurrentAndShow(supportFragmentManager, "targetTypePicker")
    }

    private fun pickNumericalFrequency() {
        ChoiceDialog.newInstance(
            listOf(getString(R.string.every_day), getString(R.string.every_week), getString(R.string.every_month))
        ) { which ->
            update { it.copy(freqDen = denominatorForChoice(which)) }
        }.dismissCurrentAndShow(supportFragmentManager, "numericalFrequencyPicker")
    }

    private fun pickTime() {
        val current = form
        val (hour, minute) = reminderStartTime(current.reminderHour, current.reminderMin)
        val androidColor = themeSwitcher.currentTheme.color(current.color).toInt()
        val dialog = TimePickerDialog.newInstance(
            object : TimePickerDialog.OnTimePickedListener {
                override fun onTimePicked(hour: Int, minute: Int) {
                    update { it.copy(reminderHour = hour, reminderMin = minute) }
                }

                override fun onTimeCleared() {
                    update { it.clearReminder() }
                }
            },
            hour,
            minute,
            DateFormat.is24HourFormat(this),
            androidColor,
            showClear = true
        )
        dialog.dismissCurrentAndShow(supportFragmentManager, "timePicker")
    }

    private fun pickDays() {
        val dialog = WeekdayPickerDialog()
        dialog.setListener { days ->
            update { it.copy(reminderDays = if (days.isEmpty) WeekdayList.EVERY_DAY else days) }
        }
        dialog.setSelectedDays(form.reminderDays)
        dialog.dismissCurrentAndShow(supportFragmentManager, "dayPicker")
    }

    private fun save(form: EditHabitForm) {
        val result = saver.save(form, habitId.takeIf { it >= 0 })
        // A second tap before the screen closes is ignored; a habit that is gone closes the screen.
        if (result != SaveResult.ALREADY_SAVING) finish()
    }
}
