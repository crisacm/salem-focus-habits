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

import android.content.res.Resources
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
import com.android.datetimepicker.time.RadialPickerLayout
import com.android.datetimepicker.time.TimePickerDialog
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.AndroidThemeSwitcher
import io.github.crisacm.salem.activities.common.dialogs.ChoiceDialog
import io.github.crisacm.salem.activities.common.dialogs.ColorPickerDialogFactory
import io.github.crisacm.salem.activities.common.dialogs.FrequencyLabel
import io.github.crisacm.salem.activities.common.dialogs.FrequencyPickerDialog
import io.github.crisacm.salem.activities.common.dialogs.WeekdayPickerDialog
import io.github.crisacm.salem.activities.common.dialogs.frequencyLabel
import io.github.crisacm.salem.core.commands.CreateHabitCommand
import io.github.crisacm.salem.core.commands.EditHabitCommand
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.ui.compose.SalemTheme
import io.github.crisacm.salem.utils.dismissCurrentAndShow

fun formatFrequency(freqNum: Int, freqDen: Int, resources: Resources) = when (val label = frequencyLabel(freqNum, freqDen)) {
    FrequencyLabel.EveryMonth -> resources.getString(R.string.every_month)
    is FrequencyLabel.TimesPerMonth -> resources.getString(R.string.x_times_per_month, label.times)
    FrequencyLabel.EveryDay -> resources.getString(R.string.every_day)
    FrequencyLabel.EveryWeek -> resources.getString(R.string.every_week)
    is FrequencyLabel.EveryXDays -> resources.getString(R.string.every_x_days, label.days)
    is FrequencyLabel.TimesPerWeek -> resources.getString(R.string.x_times_per_week, label.times)
    is FrequencyLabel.TimesInDays -> resources.getString(R.string.x_times_per_y_days, label.times, label.days)
}

/**
 * Screen that creates a habit (extra `habitType`) or edits one (extra `habitId`).
 */
class EditHabitActivity : AppCompatActivity() {
    private lateinit var themeSwitcher: AndroidThemeSwitcher
    private var habitId = -1L
    private var formState: MutableState<EditHabitForm>? = null
    private var saving = false

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val component = (application as HabitsApplication).component
        themeSwitcher = AndroidThemeSwitcher(this, component.preferences)
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
            val den = when (which) {
                1 -> 7
                2 -> 30
                else -> 1
            }
            update { it.copy(freqDen = den) }
        }.dismissCurrentAndShow(supportFragmentManager, "numericalFrequencyPicker")
    }

    private fun pickTime() {
        val current = form
        val hour = if (current.reminderHour >= 0) current.reminderHour else 8
        val minute = if (current.reminderMin >= 0) current.reminderMin else 0
        val androidColor = themeSwitcher.currentTheme.color(current.color).toInt()
        val dialog = TimePickerDialog.newInstance(
            object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: RadialPickerLayout?, hourOfDay: Int, minute: Int) {
                    update { it.copy(reminderHour = hourOfDay, reminderMin = minute) }
                }

                override fun onTimeCleared(view: RadialPickerLayout?) {
                    update { it.clearReminder() }
                }
            },
            hour,
            minute,
            DateFormat.is24HourFormat(this),
            androidColor
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
        // A second tap before the screen closes must not create the habit twice.
        if (saving) return
        saving = true
        val component = (application as HabitsApplication).component
        val habit = component.modelFactory.buildHabit()
        if (habitId >= 0) {
            val original = component.habitList.getById(habitId)
            if (original == null) {
                finish()
                return
            }
            habit.copyFrom(original)
        }
        form.applyTo(habit)
        val command = if (habitId >= 0) {
            EditHabitCommand(component.habitList, habitId, habit)
        } else {
            CreateHabitCommand(component.modelFactory, component.habitList, habit)
        }
        component.commandRunner.run(command)
        finish()
    }
}
