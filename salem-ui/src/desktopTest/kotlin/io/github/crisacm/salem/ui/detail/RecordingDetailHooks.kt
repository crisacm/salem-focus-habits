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

package io.github.crisacm.salem.ui.detail

import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.callbacks.CheckMarkDialogCallback
import io.github.crisacm.salem.core.ui.callbacks.NumberPickerCallback
import io.github.crisacm.salem.core.ui.callbacks.OnConfirmedCallback
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitState
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.platform.time.LocalDate

/** Records what the session asks of the platform. */
internal class RecordingDetailHooks : HabitDetailSession.Hooks {
    val states = mutableListOf<ShowHabitState>()
    val messages = mutableListOf<ShowHabitMenuPresenter.Message?>()
    var widgetUpdates = 0
    var feedback = 0
    var closed = 0
    var edited: Habit? = null
    var confirmation: OnConfirmedCallback? = null
    var historyEditorListener: OnDateClickedListener? = null
    var sentFile: String? = null

    override fun onStateChanged(state: ShowHabitState) {
        states += state
    }

    override fun updateWidgets() {
        widgetUpdates++
    }

    override fun showFeedback() {
        feedback++
    }

    override fun showHistoryEditorDialog(listener: OnDateClickedListener) {
        historyEditorListener = listener
    }

    override fun showNumberPopup(habit: Habit, date: LocalDate, value: Double, notes: String, callback: NumberPickerCallback) {}

    override fun showCheckmarkPopup(selectedValue: Int, notes: String, color: PaletteColor, callback: CheckMarkDialogCallback) {}

    override fun showEditHabitScreen(habit: Habit) {
        edited = habit
    }

    override fun showMessage(m: ShowHabitMenuPresenter.Message?) {
        messages += m
    }

    override fun showSendFileScreen(filename: String) {
        sentFile = filename
    }

    override fun showDeleteConfirmationScreen(callback: OnConfirmedCallback) {
        confirmation = callback
    }

    override fun close() {
        closed++
    }
}
