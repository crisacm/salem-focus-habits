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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.core.ui.screens.habits.show.views.BarCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardState
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.detail.charts.BarHistoryChart
import io.github.crisacm.salem.ui.detail.charts.CalendarDotsChart
import io.github.crisacm.salem.ui.detail.charts.barAxisLabel
import io.github.crisacm.salem.ui.detail.charts.barColumns
import io.github.crisacm.salem.ui.detail.charts.calendarColumns
import io.github.crisacm.salem.ui.detail.charts.rangeLabel
import io.github.crisacm.salem.ui.detail.charts.rememberChartScroll
import io.github.crisacm.salem.ui.detail.charts.weekdayLetters
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.calendar
import io.github.crisacm.salem.ui.resources.edit
import io.github.crisacm.salem.ui.resources.history
import org.jetbrains.compose.resources.stringResource

/** The calendar card: a dot for each day of the history, scrolling back in time, and a button that opens the editor. */
@Composable
fun HistoryCardContent(state: HistoryCardState, onEdit: () -> Unit, dateFormatter: LocalDateFormatter) {
    val tones = rememberDetailTones(state.theme, state.color)
    val columns = remember(state) { calendarColumns(state.series, state.defaultSquare, state.today, state.firstWeekday) }
    val letters = remember(state.firstWeekday, dateFormatter) { weekdayLetters(state.firstWeekday, dateFormatter::narrowWeekdayName) }
    val scroll = rememberChartScroll()
    Column(Modifier.fillMaxWidth().testTag("historyCard")) {
        CardHeader(stringResource(Res.string.calendar))
        CalendarDotsChart(
            columns = columns,
            letters = letters,
            scroll = scroll,
            tones = tones,
            monthName = dateFormatter::shortMonthName,
            modifier = Modifier.testTag("historyChart")
        )
        Button(
            onClick = onEdit,
            colors = ButtonDefaults.buttonColors(containerColor = tones.container, contentColor = tones.accent),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp).testTag("historyEdit")
        ) {
            Text(stringResource(Res.string.edit).uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BarCardContent(
    state: BarCardState,
    onBoolSpinner: (Int) -> Unit,
    onNumericalSpinner: (Int) -> Unit,
    dateFormatter: LocalDateFormatter
) {
    val tones = rememberDetailTones(state.theme, state.color)
    val bars = remember(state.entries) { barColumns(state.entries) }
    // The chart starts at the newest data again when the bucket changes
    val scroll = rememberChartScroll(state.bucketSize)
    val visible = scroll.visible()
    val period = if (bars.isEmpty() || visible.isEmpty()) {
        null
    } else {
        rangeLabel(bars[visible.last].date, bars[visible.first].date, dateFormatter::shortMonthName)
    }
    val entries = intervalNames(withDay = state.isNumerical).map { stringResource(it) }
    Column(Modifier.fillMaxWidth().testTag("barCard")) {
        CardHeader(stringResource(Res.string.history), period) {
            PillSpinner(
                entries = entries,
                selected = if (state.isNumerical) state.numericalSpinnerPosition else state.boolSpinnerPosition,
                onSelected = if (state.isNumerical) onNumericalSpinner else onBoolSpinner
            )
        }
        BarHistoryChart(
            bars = bars,
            bucketSize = state.bucketSize,
            scroll = scroll,
            tones = tones,
            axisLabel = { bucketSize, date -> barAxisLabel(bucketSize, date, dateFormatter::shortMonthName) },
            modifier = Modifier.testTag("barChart")
        )
    }
}
