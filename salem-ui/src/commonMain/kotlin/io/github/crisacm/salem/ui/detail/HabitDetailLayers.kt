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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.callbacks.CheckMarkDialogCallback
import io.github.crisacm.salem.core.ui.callbacks.NumberPickerCallback
import io.github.crisacm.salem.core.ui.callbacks.OnConfirmedCallback
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.list.ListOverlays

/** The messages that the detail shows in a banner. */
enum class DetailMessage { ARCHIVED, UNARCHIVED, COULD_NOT_EXPORT }

/** Which habit the app shows in its detail screen, if any: the screen over the habit list. */
class DetailNavigation {
    var habit by mutableStateOf<Habit?>(null)
        private set

    fun open(habit: Habit) {
        this.habit = habit
    }

    fun close() {
        habit = null
    }
}

/**
 * The [HabitDetailSession.Hooks] of a platform that draws the dialogs itself: the popups, the editor and the delete
 * confirmation are the [overlays], the history editor is a layer of its own (a popup of a day opens above it), and
 * the messages go to a banner. [onClose] leaves the detail.
 */
class HabitDetailLayers(
    private val onClose: () -> Unit,
    private val onShare: (String) -> Unit
) : HabitDetailSession.Hooks {
    val overlays = ListOverlays()
    val historyEditor = HistoryEditorLayer()

    var message by mutableStateOf<DetailMessage?>(null)
        private set

    /** Changes every time a message is shown, so showing the same one again restarts its timer. */
    var messageVersion by mutableIntStateOf(0)
        private set

    /** Whether something is drawn over the detail screen, which then hides from accessibility. */
    val isCovering: Boolean get() = overlays.current != null || historyEditor.isOpen

    fun show(message: DetailMessage) {
        this.message = message
        messageVersion++
    }

    fun clearMessage() {
        message = null
    }

    override fun updateWidgets() {}

    override fun showFeedback() {}

    override fun showHistoryEditorDialog(listener: OnDateClickedListener) = historyEditor.show(listener)

    override fun showNumberPopup(habit: Habit, date: LocalDate, value: Double, notes: String, callback: NumberPickerCallback) =
        overlays.showNumber(habit, date, value, notes, callback)

    override fun showCheckmarkPopup(
        habit: Habit,
        date: LocalDate,
        selectedValue: Int,
        notes: String,
        color: PaletteColor,
        callback: CheckMarkDialogCallback
    ) = overlays.showCheckmark(habit, date, selectedValue, notes, color, callback)

    override fun showEditHabitScreen(habit: Habit) = overlays.showEditor(habit.id, habit.type)

    override fun showMessage(m: ShowHabitMenuPresenter.Message?) {
        when (m) {
            ShowHabitMenuPresenter.Message.HABIT_ARCHIVED -> show(DetailMessage.ARCHIVED)
            ShowHabitMenuPresenter.Message.HABIT_UNARCHIVED -> show(DetailMessage.UNARCHIVED)
            ShowHabitMenuPresenter.Message.COULD_NOT_EXPORT -> show(DetailMessage.COULD_NOT_EXPORT)
            null -> {}
        }
    }

    override fun showSendFileScreen(filename: String) = onShare(filename)

    override fun showDeleteConfirmationScreen(callback: OnConfirmedCallback) = overlays.showDelete(1, callback)

    override fun close() = onClose()
}
