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

package io.github.crisacm.salem.core.ui.screens.habits.show

import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.screens.habits.show.views.BarCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.BarCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.FrequencyCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.FrequencyCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.NotesCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.NotesCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.OverviewCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.OverviewCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.ScoreCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.ScoreCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.StreakCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.StreakCartPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.SubtitleCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.SubtitleCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.TargetCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.TargetCardState
import io.github.crisacm.salem.core.ui.views.Theme

data class ShowHabitState(
    val title: String = "",
    val isNumerical: Boolean = false,
    val color: PaletteColor = PaletteColor(1),
    val subtitle: SubtitleCardState,
    val overview: OverviewCardState,
    val notes: NotesCardState,
    val target: TargetCardState,
    val streaks: StreakCardState,
    val scores: ScoreCardState,
    val frequency: FrequencyCardState,
    val history: HistoryCardState,
    val bar: BarCardState,
    val theme: Theme,
    // What the icon of the habit is chosen from, so the detail shows the same icon as the list
    val habitUuid: String? = null,
    val habitId: Long? = null
)

class ShowHabitPresenter(
    val habit: Habit,
    val habitList: HabitList,
    val preferences: Preferences,
    val screen: Screen,
    val commandRunner: CommandRunner
) {
    val historyCardPresenter = HistoryCardPresenter(
        commandRunner = commandRunner,
        habit = habit,
        habitList = habitList,
        preferences = preferences,
        screen = screen
    )

    val barCardPresenter = BarCardPresenter(
        preferences = preferences,
        screen = screen
    )

    val scoreCardPresenter = ScoreCardPresenter(
        preferences = preferences,
        screen = screen
    )

    companion object {
        fun buildState(
            habit: Habit,
            preferences: Preferences,
            theme: Theme
        ): ShowHabitState {
            return ShowHabitState(
                title = habit.name,
                habitUuid = habit.uuid,
                habitId = habit.id,
                color = habit.color,
                isNumerical = habit.isNumerical,
                theme = theme,
                subtitle = SubtitleCardPresenter.buildState(
                    habit = habit,
                    theme = theme
                ),
                overview = OverviewCardPresenter.buildState(
                    habit = habit,
                    theme = theme
                ),
                notes = NotesCardPresenter.buildState(
                    habit = habit
                ),
                target = TargetCardPresenter.buildState(
                    habit = habit,
                    firstWeekday = preferences.firstWeekdayInt,
                    theme = theme
                ),
                streaks = StreakCartPresenter.buildState(
                    habit = habit,
                    theme = theme
                ),
                scores = ScoreCardPresenter.buildState(
                    spinnerPosition = preferences.scoreCardSpinnerPosition,
                    habit = habit,
                    firstWeekday = preferences.firstWeekdayInt,
                    theme = theme
                ),
                frequency = FrequencyCardPresenter.buildState(
                    habit = habit,
                    firstWeekday = preferences.firstWeekday,
                    theme = theme
                ),
                history = HistoryCardPresenter.buildState(
                    habit = habit,
                    firstWeekday = preferences.firstWeekday,
                    theme = theme
                ),
                bar = BarCardPresenter.buildState(
                    habit = habit,
                    firstWeekday = preferences.firstWeekdayInt,
                    boolSpinnerPosition = preferences.barCardBoolSpinnerPosition,
                    numericalSpinnerPosition = preferences.barCardNumericalSpinnerPosition,
                    theme = theme
                )
            )
        }
    }

    interface Screen :
        BarCardPresenter.Screen,
        ScoreCardPresenter.Screen,
        HistoryCardPresenter.Screen
}
