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

package io.github.crisacm.salem.core.ui.screens.habits.edit

import io.github.crisacm.salem.core.models.Frequency
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.Reminder
import io.github.crisacm.salem.core.models.WeekdayList

/** What the form of the create and edit habit screen can be wrong about. */
data class EditHabitErrors(val nameBlank: Boolean, val targetBlank: Boolean) {
    val isValid get() = !nameBlank && !targetBlank
}

/**
 * The state of the create and edit habit screen. [reminderHour] below zero means no reminder.
 * [targetText] is what the user typed; only numerical habits use it.
 */
data class EditHabitForm(
    val type: HabitType,
    val name: String = "",
    val question: String = "",
    val notes: String = "",
    val unit: String = "",
    val targetText: String = "",
    val targetType: NumericalHabitType = NumericalHabitType.AT_LEAST,
    val color: PaletteColor = DEFAULT_COLOR,
    val freqNum: Int = 1,
    val freqDen: Int = 1,
    val reminderHour: Int = -1,
    val reminderMin: Int = -1,
    val reminderDays: WeekdayList = WeekdayList.EVERY_DAY
) {
    val hasReminder get() = reminderHour >= 0

    /**
     * The form as a habit of [newType]. A numerical habit only has a frequency per day, week or month, so a
     * frequency of a yes or no habit that is not one of them (such as 3 times per week) becomes every day.
     */
    fun withType(newType: HabitType): EditHabitForm = when {
        newType == type -> this
        newType == HabitType.NUMERICAL -> copy(
            type = newType,
            freqNum = 1,
            freqDen = if (freqDen in NUMERICAL_DENOMINATORS) freqDen else 1
        )
        else -> copy(type = newType)
    }

    fun clearReminder() = copy(reminderHour = -1, reminderMin = -1, reminderDays = WeekdayList.EVERY_DAY)

    fun validate() = EditHabitErrors(
        nameBlank = name.isBlank(),
        targetBlank = type == HabitType.NUMERICAL && parseTarget() == null
    )

    /** Copies the form into [habit]. The target fields are only touched for numerical habits. */
    fun applyTo(habit: Habit) {
        habit.name = name.trim()
        habit.question = question.trim()
        habit.description = notes.trim()
        habit.color = color
        habit.reminder = if (hasReminder) Reminder(reminderHour, reminderMin, reminderDays) else null
        habit.frequency = Frequency(freqNum, freqDen)
        if (type == HabitType.NUMERICAL) {
            parseTarget()?.let { habit.targetValue = it }
            habit.targetType = targetType
            habit.unit = unit.trim()
        }
        habit.type = type
    }

    // Both the dot and the comma are accepted as the decimal separator; NaN and infinity are not numbers here.
    private fun parseTarget() = targetText.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

    companion object {
        val DEFAULT_COLOR = PaletteColor(11)

        /** The periods a numerical habit can have: a day, a week and a month. */
        private val NUMERICAL_DENOMINATORS = listOf(1, 7, 30)

        fun new(type: HabitType) = EditHabitForm(type)

        fun from(habit: Habit) = EditHabitForm(
            type = habit.type,
            name = habit.name,
            question = habit.question,
            notes = habit.description,
            unit = habit.unit,
            targetText = habit.targetValue.toString(),
            targetType = habit.targetType,
            color = habit.color,
            freqNum = habit.frequency.numerator,
            freqDen = habit.frequency.denominator,
            reminderHour = habit.reminder?.hour ?: -1,
            reminderMin = habit.reminder?.minute ?: -1,
            reminderDays = habit.reminder?.days ?: WeekdayList.EVERY_DAY
        )
    }
}
