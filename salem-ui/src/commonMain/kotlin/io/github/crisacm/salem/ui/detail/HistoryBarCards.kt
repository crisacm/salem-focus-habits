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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.ui.screens.habits.show.views.BarCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardState
import io.github.crisacm.salem.core.ui.views.BarChart
import io.github.crisacm.salem.core.ui.views.HistoryChart
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.canvas.CoreChart
import io.github.crisacm.salem.ui.compose.ChartScrollState
import io.github.crisacm.salem.ui.compose.toCompose
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.calendar
import io.github.crisacm.salem.ui.resources.edit
import io.github.crisacm.salem.ui.resources.history
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoryCardContent(state: HistoryCardState, onEdit: () -> Unit, dateFormatter: LocalDateFormatter) {
    val color = state.theme.color(state.color).toCompose()
    val scroll = remember { ChartScrollState() }
    val chart = remember(state) {
        HistoryChart(
            today = state.today,
            paletteColor = state.color,
            theme = state.theme,
            dateFormatter = dateFormatter,
            series = state.series,
            defaultSquare = state.defaultSquare,
            notesIndicators = state.notesIndicators,
            firstWeekday = state.firstWeekday
        )
    }
    Column(Modifier.fillMaxWidth().testTag("historyCard")) {
        CardTitle(stringResource(Res.string.calendar), color)
        CoreChart(chart, scroll, modifier = Modifier.fillMaxWidth().height(160.dp).testTag("historyChart"))
        TextButton(onClick = onEdit, modifier = Modifier.align(Alignment.CenterHorizontally).testTag("historyEdit")) {
            Text(stringResource(Res.string.edit), color = GREY_400, fontSize = SMALL_TEXT)
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
    val color = state.theme.color(state.color).toCompose()
    val scroll = remember { ChartScrollState() }
    val chart = remember(state) {
        scroll.reset()
        BarChart(state.theme, dateFormatter).apply {
            series = mutableListOf(state.entries.map { it.value / 1000.0 })
            colors = mutableListOf(theme.color(state.color.paletteIndex))
            axis = state.entries.map { it.date }
        }
    }
    Box(Modifier.fillMaxWidth().testTag("barCard")) {
        Column {
            CardTitle(stringResource(Res.string.history), color)
            CoreChart(chart, scroll, modifier = Modifier.fillMaxWidth().height(220.dp).testTag("barChart"))
        }
        if (state.isNumerical) {
            CardSpinner(
                entries = intervalNames(withDay = true).map { stringResource(it) },
                selected = state.numericalSpinnerPosition,
                onSelected = onNumericalSpinner,
                color = color,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        } else {
            CardSpinner(
                entries = intervalNames(withDay = false).map { stringResource(it) },
                selected = state.boolSpinnerPosition,
                onSelected = onBoolSpinner,
                color = color,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}
