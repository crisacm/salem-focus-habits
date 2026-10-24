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

package io.github.crisacm.salem.activities.common.dialogs

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.AndroidThemeSwitcher
import io.github.crisacm.salem.core.commands.Command
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardState
import io.github.crisacm.salem.core.ui.views.HistoryChart
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.compose.ChartScrollState
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.ui.compose.CoreChart
import io.github.crisacm.salem.ui.compose.Scrim
import java.util.Locale

/**
 * The calendar of a habit in a dialog, to edit the history: a tap on a day goes to the listener that
 * [ShowHabitActivity][io.github.crisacm.salem.activities.habits.show.ShowHabitActivity] gives it, which
 * opens the popup of that day. The activity gives the listener again after it is recreated, so this
 * dialog is not dismissed when it is restored. It is shown with its own tag so that other popups can sit
 * above it.
 */
class HistoryEditorDialog : ComposeDialogFragment(), CommandRunner.Listener {

    private lateinit var commandRunner: CommandRunner
    private lateinit var preferences: Preferences
    private lateinit var themeSwitcher: AndroidThemeSwitcher
    private var habit: Habit? = null
    private var state by mutableStateOf<HistoryCardState?>(null)
    private var onDateClickedListener: OnDateClickedListener? = null

    // The chart keeps this one; it reads the current listener, which can change after a recreation.
    private val listenerAdapter = object : OnDateClickedListener {
        override fun onDateShortPress(date: LocalDate) {
            onDateClickedListener?.onDateShortPress(date)
        }

        override fun onDateLongPress(date: LocalDate) {
            onDateClickedListener?.onDateLongPress(date)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (requireActivity().application as HabitsApplication).component
        commandRunner = component.commandRunner
        preferences = component.preferences
        themeSwitcher = AndroidThemeSwitcher(requireActivity(), preferences)
        themeSwitcher.apply()
        habit = component.habitList.getById(requireArguments().getLong("habit"))
        if (habit == null) dismissAllowingStateLoss()
    }

    @Composable
    override fun Content() {
        val current = state ?: return
        val scroll = remember { ChartScrollState() }
        val chart = remember(current) {
            HistoryChart(
                dateFormatter = JavaLocalDateFormatter(Locale.getDefault()),
                firstWeekday = preferences.firstWeekday,
                paletteColor = current.color,
                series = current.series,
                defaultSquare = current.defaultSquare,
                notesIndicators = current.notesIndicators,
                theme = current.theme,
                today = current.today,
                onDateClickedListener = listenerAdapter,
                padding = 10.0
            )
        }
        val height = historyEditorHeight(
            screenHeight = LocalConfiguration.current.screenHeightDp.dp,
            maxHeight = dimensionResource(R.dimen.history_editor_max_height)
        )
        Scrim(onDismiss = { dismissAllowingStateLoss() }) {
            CoreChart(
                chart = chart,
                scroll = scroll,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(current.theme.cardBackgroundColor.toInt()))
                    // Taps on the calendar must not reach the scrim, which dismisses the dialog.
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                    .testTag("historyEditorChart")
            )
        }
    }

    override fun onResume() {
        super.onResume()
        commandRunner.addListener(this)
        refreshData()
    }

    override fun onPause() {
        commandRunner.removeListener(this)
        super.onPause()
    }

    fun setOnDateClickedListener(listener: OnDateClickedListener) {
        onDateClickedListener = listener
    }

    private fun refreshData() {
        val habit = habit ?: return
        state = HistoryCardPresenter.buildState(habit, preferences.firstWeekday, themeSwitcher.currentTheme)
    }

    override fun onCommandFinished(command: Command) {
        refreshData()
    }
}

/** The calendar takes the height of the window, up to [maxHeight]. */
fun historyEditorHeight(screenHeight: androidx.compose.ui.unit.Dp, maxHeight: androidx.compose.ui.unit.Dp) =
    if (screenHeight < maxHeight) screenHeight else maxHeight
