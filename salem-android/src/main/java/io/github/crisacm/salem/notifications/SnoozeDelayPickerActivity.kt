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

package io.github.crisacm.salem.notifications

import android.content.ContentUris
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.AndroidThemeSwitcher
import io.github.crisacm.salem.activities.common.dialogs.TimePickerDialog
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.receivers.ReminderController
import io.github.crisacm.salem.ui.compose.DialogSurface
import io.github.crisacm.salem.ui.compose.SalemTheme
import io.github.crisacm.salem.utils.SystemUtils
import java.util.Calendar

class SnoozeDelayPickerActivity : FragmentActivity() {
    private var habit: Habit? = null
    private var reminderController: ReminderController? = null
    private var androidColor: Int = 0

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        val data = intent?.data
        if (data == null) {
            finish()
            return
        }
        val app = applicationContext as HabitsApplication
        val appComponent = app.component
        val themeSwitcher = AndroidThemeSwitcher(this, appComponent.preferences)
        themeSwitcher.setTheme()

        val habit = appComponent.habitList.getById(ContentUris.parseId(data))
        if (habit == null) {
            finish()
            return
        }
        this.habit = habit
        androidColor = themeSwitcher.currentTheme.color(habit.color).toInt()
        reminderController = appComponent.reminderController
        setContent {
            SalemTheme(themeSwitcher.currentTheme) {
                val names = stringArrayResource(R.array.snooze_picker_names)
                val values = resources.getIntArray(R.array.snooze_picker_values)
                DialogSurface(title = stringResource(R.string.select_snooze_delay), onDismiss = ::finish) {
                    names.forEachIndexed { index, name ->
                        Text(
                            name,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChoice(snoozeChoice(values[index])) }
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }
                }
            }
        }
        SystemUtils.unlockScreen(this)
    }

    // The context theme is switched after super.onCreate on purpose: the window keeps the translucent
    // theme of the manifest and only the dialogs inflated later (the time picker) use the dialog theme.
    private fun AndroidThemeSwitcher.setTheme() {
        if (this.isNightMode) {
            setTheme(R.style.BaseDialogDark)
            this.currentTheme = DarkTheme()
        } else {
            setTheme(R.style.BaseDialog)
            this.currentTheme = LightTheme()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val dialog = TimePickerDialog.newInstance(
            { hour: Int, minute: Int ->
                reminderController?.onSnoozeTimePicked(habit, hour, minute)
                finish()
            },
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            DateFormat.is24HourFormat(this),
            androidColor
        )
        dialog.show(supportFragmentManager, "timePicker")
    }

    private fun onChoice(choice: SnoozeChoice) {
        when (choice) {
            is SnoozeChoice.Delay -> {
                habit?.let { reminderController?.onSnoozeDelayPicked(it, choice.minutes) }
                finish()
            }
            SnoozeChoice.CustomTime -> showTimePicker()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
