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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.ui.screens.habits.show.views.FrequencyCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.ScoreCardState
import io.github.crisacm.salem.core.ui.views.FrequencyChart
import io.github.crisacm.salem.core.ui.views.ScoreChart
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.canvas.CoreChart
import io.github.crisacm.salem.ui.compose.ChartScrollState
import io.github.crisacm.salem.ui.compose.toCompose
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.frequency
import io.github.crisacm.salem.ui.resources.score
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScoreCardContent(state: ScoreCardState, onSpinner: (Int) -> Unit, dateFormatter: LocalDateFormatter) {
    val color = state.theme.color(state.color).toCompose()
    val scroll = remember { ChartScrollState() }
    val chart = remember(state) {
        scroll.reset()
        ScoreChart(
            theme = state.theme,
            dateFormatter = dateFormatter,
            scores = state.scores,
            color = state.theme.color(state.color),
            bucketSize = state.bucketSize
        )
    }
    Box(Modifier.fillMaxWidth().testTag("scoreCard")) {
        Column {
            CardTitle(stringResource(Res.string.score), color)
            CoreChart(chart, scroll, modifier = Modifier.fillMaxWidth().height(220.dp).testTag("scoreChart"))
        }
        CardSpinner(
            entries = intervalNames(withDay = true).map { stringResource(it) },
            selected = state.spinnerPosition,
            onSelected = onSpinner,
            color = color,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
fun FrequencyCardContent(state: FrequencyCardState, dateFormatter: LocalDateFormatter) {
    val color = state.theme.color(state.color).toCompose()
    val scroll = remember { ChartScrollState() }
    val chart = remember(state) {
        FrequencyChart(
            theme = state.theme,
            dateFormatter = dateFormatter,
            frequency = state.frequency,
            color = state.theme.color(state.color),
            isNumerical = state.isNumerical,
            firstWeekday = state.firstWeekday
        )
    }
    Column(Modifier.fillMaxWidth().testTag("frequencyCard")) {
        CardTitle(stringResource(Res.string.frequency), color)
        CoreChart(chart, scroll, modifier = Modifier.fillMaxWidth().height(200.dp).testTag("frequencyChart"))
    }
}
