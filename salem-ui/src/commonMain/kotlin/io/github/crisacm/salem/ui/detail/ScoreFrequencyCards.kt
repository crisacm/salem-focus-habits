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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.crisacm.salem.core.ui.screens.habits.show.views.FrequencyCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.ScoreCardState
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.detail.charts.ScoreLineChart
import io.github.crisacm.salem.ui.detail.charts.WeekdayBarsChart
import io.github.crisacm.salem.ui.detail.charts.rangeLabel
import io.github.crisacm.salem.ui.detail.charts.rememberChartScroll
import io.github.crisacm.salem.ui.detail.charts.scoreAxisLabel
import io.github.crisacm.salem.ui.detail.charts.scorePoints
import io.github.crisacm.salem.ui.detail.charts.weekdayBars
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.completions_by_weekday
import io.github.crisacm.salem.ui.resources.frequency
import io.github.crisacm.salem.ui.resources.score
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScoreCardContent(state: ScoreCardState, onSpinner: (Int) -> Unit, dateFormatter: LocalDateFormatter) {
    val tones = rememberDetailTones(state.theme, state.color)
    val points = remember(state.scores) { scorePoints(state.scores) }
    // The chart starts at the newest data again when the bucket changes
    val scroll = rememberChartScroll(state.bucketSize)
    val visible = scroll.visible()
    val period = if (points.isEmpty() || visible.isEmpty()) {
        null
    } else {
        rangeLabel(points[visible.last].date, points[visible.first].date, dateFormatter::shortMonthName)
    }
    val entries = intervalNames(withDay = true).map { stringResource(it) }
    Column(Modifier.fillMaxWidth().testTag("scoreCard")) {
        CardHeader(stringResource(Res.string.score), period) {
            PillSpinner(entries, state.spinnerPosition, onSpinner)
        }
        ScoreLineChart(
            points = points,
            bucketSize = state.bucketSize,
            scroll = scroll,
            tones = tones,
            axisLabel = { scoreAxisLabel(state.bucketSize, it, dateFormatter::shortMonthName) },
            modifier = Modifier.testTag("scoreChart")
        )
    }
}

@Composable
fun FrequencyCardContent(state: FrequencyCardState, dateFormatter: LocalDateFormatter) {
    val tones = rememberDetailTones(state.theme, state.color)
    val bars = remember(state) { weekdayBars(state.frequency, state.isNumerical, state.firstWeekday) }
    Column(Modifier.fillMaxWidth().testTag("frequencyCard")) {
        CardHeader(stringResource(Res.string.frequency), stringResource(Res.string.completions_by_weekday))
        WeekdayBarsChart(bars, tones, dateFormatter::shortWeekdayName, Modifier.testTag("frequencyChart"))
    }
}
