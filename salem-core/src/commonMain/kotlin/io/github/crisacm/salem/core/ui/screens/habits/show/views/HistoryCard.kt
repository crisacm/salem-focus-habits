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

package io.github.crisacm.salem.core.ui.screens.habits.show.views

import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.commands.CreateRepetitionCommand
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.Entry.Companion.SKIP
import io.github.crisacm.salem.core.models.Entry.Companion.YES_AUTO
import io.github.crisacm.salem.core.models.Entry.Companion.YES_MANUAL
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.callbacks.CheckMarkDialogCallback
import io.github.crisacm.salem.core.ui.callbacks.NumberPickerCallback
import io.github.crisacm.salem.core.ui.views.HistoryChart
import io.github.crisacm.salem.core.ui.views.HistoryChart.Square.DIMMED
import io.github.crisacm.salem.core.ui.views.HistoryChart.Square.GREY
import io.github.crisacm.salem.core.ui.views.HistoryChart.Square.HATCHED
import io.github.crisacm.salem.core.ui.views.HistoryChart.Square.OFF
import io.github.crisacm.salem.core.ui.views.HistoryChart.Square.ON
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.getToday
import kotlin.math.roundToInt

data class HistoryCardState(
    val color: PaletteColor,
    val firstWeekday: DayOfWeek,
    val series: List<HistoryChart.Square>,
    val defaultSquare: HistoryChart.Square,
    val notesIndicators: List<Boolean>,
    val theme: Theme,
    val today: LocalDate
)

class HistoryCardPresenter(
    val commandRunner: CommandRunner,
    val habit: Habit,
    val habitList: HabitList,
    val preferences: Preferences,
    val screen: Screen
) : OnDateClickedListener {

    override fun onDateLongPress(date: LocalDate) {
        screen.showFeedback()
        if (habit.isNumerical) {
            showNumberPopup(date)
        } else {
            if (preferences.isShortToggleEnabled) {
                showCheckmarkPopup(date)
            } else {
                toggle(date)
            }
        }
    }

    override fun onDateShortPress(date: LocalDate) {
        screen.showFeedback()
        if (habit.isNumerical) {
            showNumberPopup(date)
        } else {
            if (preferences.isShortToggleEnabled) {
                toggle(date)
            } else {
                showCheckmarkPopup(date)
            }
        }
    }

    private fun showCheckmarkPopup(date: LocalDate) {
        val entry = habit.computedEntries.get(date)
        screen.showCheckmarkPopup(
            habit,
            date,
            entry.value,
            entry.notes,
            habit.color
        ) { newValue, newNotes ->
            commandRunner.run(
                CreateRepetitionCommand(
                    habitList,
                    habit,
                    date,
                    newValue,
                    newNotes
                )
            )
        }
    }

    private fun toggle(date: LocalDate) {
        val entry = habit.computedEntries.get(date)
        val nextValue = Entry.nextToggleValue(
            value = entry.value,
            isSkipEnabled = preferences.isSkipEnabled,
            areQuestionMarksEnabled = preferences.areQuestionMarksEnabled
        )
        commandRunner.run(
            CreateRepetitionCommand(
                habitList,
                habit,
                date,
                nextValue,
                entry.notes
            )
        )
    }

    private fun showNumberPopup(date: LocalDate) {
        val entry = habit.computedEntries.get(date)
        val oldValue = entry.value
        screen.showNumberPopup(
            habit = habit,
            date = date,
            value = oldValue / 1000.0,
            notes = entry.notes
        ) { newValue: Double, newNotes: String ->
            val thousands = (newValue * 1000).roundToInt()
            commandRunner.run(
                CreateRepetitionCommand(
                    habitList,
                    habit,
                    date,
                    thousands,
                    newNotes
                )
            )
        }
    }

    fun onClickEditButton() {
        screen.showHistoryEditorDialog(this)
    }

    companion object {
        fun buildState(
            habit: Habit,
            firstWeekday: DayOfWeek,
            theme: Theme
        ): HistoryCardState {
            val today = getToday()
            val oldest = habit.computedEntries.getKnown().lastOrNull()?.date ?: today
            val entries = habit.computedEntries.getByInterval(oldest, today)
            val series = if (habit.isNumerical) {
                entries.map {
                    when {
                        it.value == SKIP -> HATCHED
                        (it.completed && it.value > 0) -> ON
                        it.completed -> DIMMED
                        it.value > 0 -> GREY
                        else -> OFF
                    }
                }
            } else {
                entries.map {
                    when (it.value) {
                        YES_MANUAL -> ON
                        YES_AUTO -> DIMMED
                        SKIP -> HATCHED
                        else -> OFF
                    }
                }
            }
            val notesIndicators = entries.map {
                when (it.notes) {
                    "" -> false
                    else -> true
                }
            }

            return HistoryCardState(
                color = habit.color,
                firstWeekday = firstWeekday,
                today = today,
                theme = theme,
                series = series,
                defaultSquare = OFF,
                notesIndicators = notesIndicators
            )
        }
    }

    interface Screen {
        fun showHistoryEditorDialog(listener: OnDateClickedListener)
        fun showFeedback()
        fun showNumberPopup(
            habit: Habit,
            date: LocalDate,
            value: Double,
            notes: String,
            callback: NumberPickerCallback
        )

        fun showCheckmarkPopup(
            habit: Habit,
            date: LocalDate,
            selectedValue: Int,
            notes: String,
            color: PaletteColor,
            callback: CheckMarkDialogCallback
        )
    }
}
