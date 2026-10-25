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
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.activities.AndroidThemeSwitcher
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm
import io.github.crisacm.salem.core.ui.screens.habits.edit.HabitEditorSaver
import io.github.crisacm.salem.core.ui.screens.habits.edit.SaveResult
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.ui.compose.SalemTheme
import io.github.crisacm.salem.ui.edit.EditHabitScreen
import io.github.crisacm.salem.ui.edit.PickerLabels
import java.util.Locale

/**
 * Screen that creates a habit (extra `habitType`) or edits one (extra `habitId`); the form and its pickers are
 * the shared [EditHabitScreen].
 */
class EditHabitActivity : AppCompatActivity() {
    private lateinit var saver: HabitEditorSaver
    private var habitId = -1L

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val component = (application as HabitsApplication).component
        val themeSwitcher = AndroidThemeSwitcher(this, component.preferences)
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

        setContent {
            val theme = themeSwitcher.currentTheme
            SalemTheme(theme) {
                EditHabitScreen(
                    initial = initial,
                    isEditing = habitId >= 0,
                    theme = theme,
                    isNight = themeSwitcher.isNightMode,
                    dateFormatter = remember { JavaLocalDateFormatter(Locale.getDefault()) },
                    onSave = ::save,
                    onBack = ::finish,
                    // The pickers use the buttons that Android already has in the language of the user.
                    labels = PickerLabels(
                        ok = stringResource(android.R.string.ok),
                        cancel = stringResource(android.R.string.cancel),
                        yes = stringResource(android.R.string.yes)
                    )
                )
            }
        }
    }

    private fun save(form: EditHabitForm) {
        val result = saver.save(form, habitId.takeIf { it >= 0 })
        // A second tap before the screen closes is ignored; a habit that is gone closes the screen.
        if (result != SaveResult.ALREADY_SAVING) finish()
    }
}
