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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.crisacm.salem.core.commands.Command
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.ui.callbacks.CheckMarkDialogCallback
import io.github.crisacm.salem.core.ui.callbacks.NumberPickerCallback
import io.github.crisacm.salem.core.ui.callbacks.OnConfirmedCallback
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitState
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.core.ui.views.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** What the cards of the detail screen ask of the presenters. */
class ShowHabitCallbacks(
    val onScoreSpinner: (Int) -> Unit = {},
    val onHistoryEdit: () -> Unit = {},
    val onBoolSpinner: (Int) -> Unit = {},
    val onNumericalSpinner: (Int) -> Unit = {}
)

/**
 * The detail of one habit: the presenters of the core, the state they build and what the menu offers. The
 * platform gives it the [Hooks] for what only it can do (popups, other screens, sharing, closing) and calls
 * [attach] and [detach] while the screen is shown, so the state follows the commands that change the habit
 * (for example Archive turns into Unarchive). [theme] is read on every refresh; [scope] must run on the main
 * thread, because the state is read by the composition. [developerOptions] shows Randomize in the menu, and
 * [exportOverride] replaces Export on a platform that cannot share the file.
 */
class HabitDetailSession(
    private val habit: Habit,
    habitList: HabitList,
    private val preferences: Preferences,
    private val commandRunner: CommandRunner,
    taskRunner: TaskRunner,
    system: ShowHabitMenuPresenter.System,
    private val theme: () -> Theme,
    private val hooks: Hooks,
    private val scope: CoroutineScope,
    developerOptions: Boolean = preferences.isDeveloper,
    private val exportOverride: (() -> Unit)? = null
) : CommandRunner.Listener, ShowHabitPresenter.Screen, ShowHabitMenuPresenter.Screen {

    /** What the session asks of the platform. */
    interface Hooks {
        /** The state was built again: the platform may repaint what is outside of the composition. */
        fun onStateChanged(state: ShowHabitState) {}

        fun updateWidgets()

        fun showFeedback()

        fun showHistoryEditorDialog(listener: OnDateClickedListener)

        fun showNumberPopup(value: Double, notes: String, callback: NumberPickerCallback)

        fun showCheckmarkPopup(selectedValue: Int, notes: String, color: PaletteColor, callback: CheckMarkDialogCallback)

        fun showEditHabitScreen(habit: Habit)

        fun showMessage(m: ShowHabitMenuPresenter.Message?)

        fun showSendFileScreen(filename: String)

        fun showDeleteConfirmationScreen(callback: OnConfirmedCallback)

        fun close()
    }

    var state by mutableStateOf<ShowHabitState?>(null)
        private set
    var canArchive by mutableStateOf(false)
        private set
    var canUnarchive by mutableStateOf(false)
        private set
    val isDeveloper = developerOptions

    private val presenter = ShowHabitPresenter(
        commandRunner = commandRunner,
        habit = habit,
        habitList = habitList,
        preferences = preferences,
        screen = this
    )

    /** The actions of the top bar. */
    val menu = ShowHabitMenuPresenter(
        commandRunner = commandRunner,
        habit = habit,
        habitList = habitList,
        screen = this,
        system = system,
        taskRunner = taskRunner
    )

    val callbacks = ShowHabitCallbacks(
        onScoreSpinner = presenter.scoreCardPresenter::onSpinnerPosition,
        onHistoryEdit = presenter.historyCardPresenter::onClickEditButton,
        onBoolSpinner = presenter.barCardPresenter::onBoolSpinnerPosition,
        onNumericalSpinner = presenter.barCardPresenter::onNumericalSpinnerPosition
    )

    /** The Export item of the menu: [exportOverride] when the platform cannot share a file, the presenter otherwise. */
    fun onExport() {
        val override = exportOverride
        if (override != null) override() else menu.onExportCSV()
    }

    /** Follows the commands that change the habit and shows its current state. */
    fun attach() {
        commandRunner.addListener(this)
        refresh()
    }

    fun detach() {
        commandRunner.removeListener(this)
    }

    /** The listener of the date clicks of the history, for a history editor that was opened before a recreation. */
    val dateClickedListener: OnDateClickedListener get() = presenter.historyCardPresenter

    override fun onCommandFinished(command: Command) = refresh()

    override fun refresh() {
        scope.launch {
            val built = ShowHabitPresenter.buildState(habit, preferences, theme())
            canArchive = menu.canArchive()
            canUnarchive = menu.canUnarchive()
            state = built
            hooks.onStateChanged(built)
        }
    }

    override fun updateWidgets() = hooks.updateWidgets()

    override fun showFeedback() = hooks.showFeedback()

    override fun showHistoryEditorDialog(listener: OnDateClickedListener) = hooks.showHistoryEditorDialog(listener)

    override fun showNumberPopup(value: Double, notes: String, callback: NumberPickerCallback) =
        hooks.showNumberPopup(value, notes, callback)

    override fun showCheckmarkPopup(selectedValue: Int, notes: String, color: PaletteColor, callback: CheckMarkDialogCallback) =
        hooks.showCheckmarkPopup(selectedValue, notes, color, callback)

    override fun showEditHabitScreen(habit: Habit) = hooks.showEditHabitScreen(habit)

    override fun showMessage(m: ShowHabitMenuPresenter.Message?) = hooks.showMessage(m)

    override fun showSendFileScreen(filename: String) = hooks.showSendFileScreen(filename)

    override fun showDeleteConfirmationScreen(callback: OnConfirmedCallback) = hooks.showDeleteConfirmationScreen(callback)

    override fun close() = hooks.close()
}
