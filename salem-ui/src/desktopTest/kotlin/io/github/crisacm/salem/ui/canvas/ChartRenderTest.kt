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

package io.github.crisacm.salem.ui.canvas

import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.screens.habits.show.views.ScoreCardPresenter.Companion.buildState
import io.github.crisacm.salem.core.ui.views.BarChart
import io.github.crisacm.salem.core.ui.views.FrequencyChart
import io.github.crisacm.salem.core.ui.views.HistoryChart
import io.github.crisacm.salem.core.ui.views.HistoryChart.Square.DIMMED
import io.github.crisacm.salem.core.ui.views.HistoryChart.Square.HATCHED
import io.github.crisacm.salem.core.ui.views.HistoryChart.Square.OFF
import io.github.crisacm.salem.core.ui.views.HistoryChart.Square.ON
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.core.ui.views.ScoreChart
import io.github.crisacm.salem.core.ui.views.StreakChart
import io.github.crisacm.salem.core.ui.views.TargetChart
import io.github.crisacm.salem.platform.gui.View
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.BaseUiTest
import org.junit.Test
import java.util.Locale

/**
 * The six charts of the detail screen drawn through [DrawScopeCanvas] against the reference images of salem-core,
 * which `JavaCanvas` made. Text is placed like `AndroidCanvas`, so the distances are not zero: each chart has its own
 * limit, about 1.4 times the measured distance, which comes from the vertical position of the text (see
 * docs/tasks/ui-charts-canvas.md).
 */
class ChartRenderTest : BaseUiTest() {
    private val theme = LightTheme()
    private val formatter = JavaLocalDateFormatter(Locale.US)

    private fun check(name: String, width: Int, height: Int, path: String, view: View, limit: Double) {
        val distance = distanceToCore(width, height, path, view)
        kotlin.test.assertTrue(distance < limit, "$name differs from $path: $distance (limit $limit)")
    }

    @Test
    fun barChart() {
        val today = LocalDate(2015, 1, 25)
        val chart = BarChart(theme, formatter)
        chart.axis = (0..100).map { today.minus(it) }
        chart.series.add(listOf(200.0, 0.0, 150.0, 137.0, 0.0, 0.0, 500.0, 30.0, 100.0, 0.0, 300.0))
        chart.colors.add(theme.color(8))
        check("BarChart", 300, 200, "views/BarChart/base.png", chart, 0.2)
    }

    @Test
    fun scoreChart() {
        val habit = fixtures.createLongHabit()
        val state = buildState(habit = habit, firstWeekday = 7, spinnerPosition = 0, theme = theme)
        val chart = ScoreChart(theme, formatter, state.scores, theme.color(state.color), state.bucketSize)
        check("ScoreChart", 300, 200, "views/ScoreChart/base.png", chart, 0.2)
    }

    @Test
    fun frequencyChart() {
        val habit = fixtures.createLongHabit()
        val chart = FrequencyChart(
            theme = theme,
            dateFormatter = formatter,
            frequency = habit.originalEntries.computeWeekdayFrequency(habit.isNumerical),
            color = theme.color(habit.color),
            isNumerical = habit.isNumerical,
            firstWeekday = DayOfWeek.SATURDAY
        )
        check("FrequencyChart", 300, 100, "views/FrequencyChart/base.png", chart, 0.6)
    }

    @Test
    fun streakChart() {
        val habit = fixtures.createLongHabit()
        val chart = StreakChart(
            theme = theme,
            formatDate = { "${formatter.shortMonthName(it)} ${it.day}, ${it.year}" },
            streaks = habit.streaks.getBest(5),
            color = theme.color(habit.color)
        )
        check("StreakChart", 300, chart.height.toInt(), "views/StreakChart/base.png", chart, 0.2)
    }

    @Test
    fun targetChart() {
        val chart = TargetChart(
            theme = theme,
            labels = listOf("Today", "Week", "Month", "Quarter", "Year"),
            values = listOf(1.0, 3.0, 12.0, 20.0, 150.0),
            targets = listOf(1.0, 5.0, 20.0, 60.0, 365.0),
            color = theme.color(8),
            formatValue = { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }
        )
        check("TargetChart", 300, chart.height.toInt(), "views/TargetChart/base.png", chart, 0.1)
    }

    @Test
    fun historyChart() {
        val series = listOf(
            2, 2, 1, 2, 1, 2, 1, 2, 2, 3, 3, 3, 3, 1, 2, 2, 1, 2, 1, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 1, 3, 3, 3, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 3, 3, 3, 1, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 1, 1, 1, 2,
            2, 2, 2, 2, 2, 1, 1, 1, 1, 2, 2, 1, 2, 1, 1, 1, 1, 1, 2, 2, 2
        ).map {
            when (it) {
                3 -> HATCHED
                2 -> ON
                1 -> DIMMED
                else -> OFF
            }
        }
        val chart = HistoryChart(
            today = LocalDate(2015, 1, 25),
            paletteColor = PaletteColor(7),
            theme = theme,
            dateFormatter = formatter,
            firstWeekday = DayOfWeek.SUNDAY,
            onDateClickedListener = object : OnDateClickedListener {
                override fun onDateShortPress(date: LocalDate) {}

                override fun onDateLongPress(date: LocalDate) {}
            },
            defaultSquare = OFF,
            series = series,
            notesIndicators = MutableList(series.size) { it % 3 == 0 }
        )
        check("HistoryChart", 400, 200, "views/HistoryChart/base.png", chart, 0.35)
    }
}
