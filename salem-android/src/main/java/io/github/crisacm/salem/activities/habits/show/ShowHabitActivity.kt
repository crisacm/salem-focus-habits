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

package io.github.crisacm.salem.activities.habits.show

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import io.github.crisacm.salem.AndroidDirFinder
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.AndroidThemeSwitcher
import io.github.crisacm.salem.activities.HabitsDirFinder
import io.github.crisacm.salem.activities.common.dialogs.CheckmarkDialog
import io.github.crisacm.salem.activities.common.dialogs.ConfirmDeleteDialog
import io.github.crisacm.salem.activities.common.dialogs.HistoryEditorDialog
import io.github.crisacm.salem.activities.common.dialogs.NumberDialog
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.callbacks.CheckMarkDialogCallback
import io.github.crisacm.salem.core.ui.callbacks.NumberPickerCallback
import io.github.crisacm.salem.core.ui.callbacks.OnConfirmedCallback
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitState
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.intents.IntentFactory
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.compose.SalemTheme
import io.github.crisacm.salem.ui.compose.usesDarkTones
import io.github.crisacm.salem.ui.detail.HabitDetailScreen
import io.github.crisacm.salem.ui.detail.HabitDetailSession
import io.github.crisacm.salem.ui.detail.habitIdFrom
import io.github.crisacm.salem.ui.list.habitTones
import io.github.crisacm.salem.utils.dismissCurrentAndShow
import io.github.crisacm.salem.utils.dismissCurrentDialog
import io.github.crisacm.salem.utils.showMessage
import io.github.crisacm.salem.utils.showSendFileScreen
import io.github.crisacm.salem.widgets.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import java.util.Locale

/**
 * The detail screen of a habit. The screen and its state are the shared [HabitDetailScreen] and
 * [HabitDetailSession]; this activity finds the habit, owns the dialogs and the status bar, and gives the session
 * the actions that only Android can do.
 */
class ShowHabitActivity : AppCompatActivity() {

    private lateinit var session: HabitDetailSession
    private lateinit var habit: Habit
    private lateinit var themeSwitcher: AndroidThemeSwitcher
    private lateinit var widgetUpdater: WidgetUpdater

    // The refreshes of the session run here; leaving the screen cancels the ones that are still queued
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = (applicationContext as HabitsApplication).component
        val habitList = appComponent.habitList
        // The habit may be gone (deleted after a widget or a notification was made) or the address may be wrong.
        val foundHabit = habitIdFrom(intent.data?.lastPathSegment)?.let(habitList::getById)
        if (foundHabit == null) {
            finish()
            return
        }
        habit = foundHabit
        widgetUpdater = appComponent.widgetUpdater

        themeSwitcher = AndroidThemeSwitcher(this, appComponent.preferences)
        themeSwitcher.apply()

        session = HabitDetailSession(
            habit = habit,
            habitList = habitList,
            preferences = appComponent.preferences,
            commandRunner = appComponent.commandRunner,
            taskRunner = appComponent.taskRunner,
            system = HabitsDirFinder(AndroidDirFinder(this)),
            theme = { themeSwitcher.currentTheme },
            hooks = Hooks(),
            scope = scope
        )
        setContent {
            SalemTheme {
                HabitDetailScreen(
                    session = session,
                    dateFormatter = remember { JavaLocalDateFormatter(Locale.getDefault()) },
                    onBack = { onSupportNavigateUp() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.findFragmentByTag("historyEditor")?.let {
            (it as HistoryEditorDialog).setOnDateClickedListener(session.dateClickedListener)
        }
        session.attach()
    }

    override fun onPause() {
        dismissCurrentDialog()
        session.detach()
        super.onPause()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private inner class Hooks : HabitDetailSession.Hooks {
        // Before Android 15 the window is not edge to edge, so the status bar needs the colour of the header; on every
        // version its icons follow the theme: dark on the light one, light on the dark ones.
        @Suppress("DEPRECATION")
        override fun onStateChanged(state: ShowHabitState) {
            val dark = state.theme.usesDarkTones()
            window.statusBarColor = habitTones(state.color.paletteIndex, dark).container.toArgb()
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !dark
        }

        override fun updateWidgets() {
            widgetUpdater.updateWidgets()
        }

        override fun showHistoryEditorDialog(listener: OnDateClickedListener) {
            // A second editor would stack above the first one.
            (supportFragmentManager.findFragmentByTag("historyEditor") as? HistoryEditorDialog)?.dismissAllowingStateLoss()
            val dialog = HistoryEditorDialog()
            dialog.arguments = Bundle().apply {
                putLong("habit", habit.id!!)
            }
            dialog.setOnDateClickedListener(listener)
            dialog.show(supportFragmentManager, "historyEditor")
        }

        override fun showFeedback() {
            window.decorView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        override fun showNumberPopup(
            habit: Habit,
            date: LocalDate,
            value: Double,
            notes: String,
            callback: NumberPickerCallback
        ) {
            val dialog = NumberDialog()
            dialog.arguments = Bundle().apply {
                putLong("habit", habit.id!!)
                putInt("date", date.daysSince2000)
                putDouble("value", value)
                putString("notes", notes)
            }
            dialog.onToggle = { v, n -> callback.onNumberPicked(v, n) }
            dialog.dismissCurrentAndShow(supportFragmentManager, "numberDialog")
        }

        override fun showCheckmarkPopup(
            selectedValue: Int,
            notes: String,
            color: PaletteColor,
            callback: CheckMarkDialogCallback
        ) {
            val theme = themeSwitcher.currentTheme
            val dialog = CheckmarkDialog()
            dialog.arguments = Bundle().apply {
                putInt("color", theme.color(color).toInt())
                putInt("value", selectedValue)
                putString("notes", notes)
            }
            dialog.onToggle = { v, n -> callback.onNotesSaved(v, n) }
            dialog.dismissCurrentAndShow(supportFragmentManager, "checkmarkDialog")
        }

        override fun showEditHabitScreen(habit: Habit) {
            startActivity(IntentFactory().startEditActivity(this@ShowHabitActivity, habit))
        }

        override fun showMessage(m: ShowHabitMenuPresenter.Message?) {
            when (m) {
                ShowHabitMenuPresenter.Message.COULD_NOT_EXPORT -> {
                    showMessage(resources.getString(R.string.could_not_export))
                }

                ShowHabitMenuPresenter.Message.HABIT_ARCHIVED -> {
                    showMessage(resources.getQuantityString(R.plurals.toast_habits_archived, 1))
                }

                ShowHabitMenuPresenter.Message.HABIT_UNARCHIVED -> {
                    showMessage(resources.getQuantityString(R.plurals.toast_habits_unarchived, 1))
                }

                else -> {}
            }
        }

        override fun showSendFileScreen(filename: String) {
            this@ShowHabitActivity.showSendFileScreen(filename)
        }

        override fun showDeleteConfirmationScreen(callback: OnConfirmedCallback) {
            ConfirmDeleteDialog.newInstance(1, callback)
                .dismissCurrentAndShow(supportFragmentManager, "confirmDelete")
        }

        override fun close() {
            this@ShowHabitActivity.finish()
        }
    }
}
