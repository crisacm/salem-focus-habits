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

import androidx.compose.runtime.saveable.listSaver
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm

/** Keeps the whole form across the recreation of the activity. */
val EditHabitFormSaver = listSaver<EditHabitForm, Any>(
    save = {
        listOf(
            it.type.value,
            it.name,
            it.question,
            it.notes,
            it.unit,
            it.targetText,
            it.targetType.value,
            it.color.paletteIndex,
            it.freqNum,
            it.freqDen,
            it.reminderHour,
            it.reminderMin,
            it.reminderDays.toInteger()
        )
    },
    restore = {
        EditHabitForm(
            type = HabitType.fromInt(it[0] as Int),
            name = it[1] as String,
            question = it[2] as String,
            notes = it[3] as String,
            unit = it[4] as String,
            targetText = it[5] as String,
            targetType = NumericalHabitType.fromInt(it[6] as Int),
            color = PaletteColor(it[7] as Int),
            freqNum = it[8] as Int,
            freqDen = it[9] as Int,
            reminderHour = it[10] as Int,
            reminderMin = it[11] as Int,
            reminderDays = WeekdayList(it[12] as Int)
        )
    }
)
