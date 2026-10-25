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

package io.github.crisacm.salem.ui.edit

import androidx.compose.runtime.saveable.SaverScope
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm
import org.junit.Assert.assertEquals
import org.junit.Test

class EditHabitFormSaverTest {
    private val scope = SaverScope { true }

    private fun roundTrip(form: EditHabitForm): EditHabitForm {
        val saved = with(EditHabitFormSaver) { scope.save(form) }!!
        return EditHabitFormSaver.restore(saved)!!
    }

    @Test
    fun aNewFormSurvivesARotation() {
        val form = EditHabitForm.new(HabitType.YES_NO)
        assertEquals(form, roundTrip(form))
    }

    @Test
    fun everyFieldIsSaved() {
        val form = EditHabitForm(
            type = HabitType.NUMERICAL,
            name = "Run",
            question = "How far?",
            notes = "Morning",
            unit = "km",
            targetText = "2,5",
            targetType = NumericalHabitType.AT_MOST,
            color = PaletteColor(4),
            freqNum = 3,
            freqDen = 7,
            reminderHour = 7,
            reminderMin = 45,
            reminderDays = WeekdayList(21)
        )
        assertEquals(form, roundTrip(form))
    }
}
