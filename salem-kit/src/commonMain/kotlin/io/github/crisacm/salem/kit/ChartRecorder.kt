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

package io.github.crisacm.salem.kit

import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitPresenter
import io.github.crisacm.salem.core.ui.views.BarChart
import io.github.crisacm.salem.core.ui.views.FrequencyChart
import io.github.crisacm.salem.core.ui.views.HistoryChart
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.core.ui.views.ScoreChart
import io.github.crisacm.salem.core.ui.views.StreakChart
import io.github.crisacm.salem.core.ui.views.TargetChart
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.gui.DataView
import io.github.crisacm.salem.platform.gui.DrawOp
import io.github.crisacm.salem.platform.gui.RecordingCanvas
import io.github.crisacm.salem.platform.gui.View
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.LocalDateFormatter

/** Builds the charts of the habit screen and records what they draw. */
class ChartRecorder(
    private val preferences: Preferences,
    private val formatter: LocalDateFormatter,
    private val theme: () -> Theme
) {
    fun height(habit: Habit, card: Int): Double {
        val view = view(habit, card, emptyList()) ?: return 0.0
        return when (view) {
            is StreakChart -> view.height
            is TargetChart -> view.height
            else -> 0.0
        }
    }

    fun draw(habit: Habit, card: Int, width: Double, height: Double, offset: Int, labels: List<String>): List<DrawOp> {
        val view = view(habit, card, labels) ?: return emptyList()
        if (view is DataView) view.dataOffset = offset
        val canvas = RecordingCanvas(width, height)
        view.draw(canvas)
        return canvas.ops
    }

    /** The day a tap on the history chart hit, or -1. */
    fun tap(habit: Habit, card: Int, x: Double, y: Double, width: Double, height: Double, offset: Int): Int {
        if (card != ChartCard.HISTORY) return -1
        val chart = view(habit, card, emptyList()) as? HistoryChart ?: return -1
        var day = -1
        chart.onDateClickedListener = object : OnDateClickedListener {
            override fun onDateShortPress(date: LocalDate) {
                day = date.daysSince2000
            }
        }
        chart.dataOffset = offset
        chart.draw(RecordingCanvas(width, height))
        chart.onClick(x, y)
        return day
    }

    private fun view(habit: Habit, card: Int, labels: List<String>): View? {
        val state = ShowHabitPresenter.buildState(habit, preferences, theme())
        val color = state.theme.color(habit.color)
        return when (card) {
            ChartCard.HISTORY -> HistoryChart(
                today = state.history.today,
                paletteColor = state.history.color,
                theme = state.theme,
                dateFormatter = formatter,
                series = state.history.series,
                defaultSquare = state.history.defaultSquare,
                notesIndicators = state.history.notesIndicators,
                firstWeekday = state.history.firstWeekday
            )
            ChartCard.SCORE -> ScoreChart(
                theme = state.theme,
                dateFormatter = formatter,
                scores = state.scores.scores,
                color = color,
                bucketSize = state.scores.bucketSize
            )
            ChartCard.FREQUENCY -> FrequencyChart(
                theme = state.theme,
                dateFormatter = formatter,
                frequency = state.frequency.frequency,
                color = color,
                isNumerical = state.frequency.isNumerical,
                firstWeekday = state.frequency.firstWeekday
            )
            ChartCard.STREAK -> StreakChart(
                state.theme,
                formatter::longFormat,
                state.streaks.bestStreaks,
                color
            )
            ChartCard.TARGET -> TargetChart(
                theme = state.theme,
                labels = targetLabels(state.target.intervals, labels),
                values = state.target.values,
                targets = state.target.targets,
                color = color,
                formatValue = { formatAmount(it) }
            )
            ChartCard.BAR -> BarChart(state.theme, formatter).apply {
                series = mutableListOf(state.bar.entries.map { it.value / 1000.0 })
                colors = mutableListOf(color)
                axis = state.bar.entries.map { it.date }
            }
            else -> null
        }
    }

    private fun targetLabels(intervals: List<Int>, labels: List<String>): List<String> {
        if (labels.size == intervals.size) return labels
        return intervals.map { interval ->
            when (interval) {
                1 -> "Today"
                7 -> "Week"
                30 -> "Month"
                91 -> "Quarter"
                else -> "Year"
            }
        }
    }
}

internal fun formatAmount(value: Double): String {
    val rounded = kotlin.math.round(value * 1000.0) / 1000.0
    val whole = rounded.toLong()
    return if (rounded == whole.toDouble()) whole.toString() else rounded.toString()
}
