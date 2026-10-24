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

package io.github.crisacm.salem.activities.habits.show.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.ui.screens.habits.show.views.FrequencyCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.ScoreCardState
import io.github.crisacm.salem.core.ui.views.FrequencyChart
import io.github.crisacm.salem.core.ui.views.ScoreChart
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.ui.compose.CardSpinner
import io.github.crisacm.salem.ui.compose.ChartScrollState
import io.github.crisacm.salem.ui.compose.CoreChart
import java.util.Locale

@Composable
fun ScoreCardContent(state: ScoreCardState, onSpinner: (Int) -> Unit) {
    val color = Color(state.theme.color(state.color).toInt())
    val scroll = remember { ChartScrollState() }
    val chart = remember(state) {
        scroll.reset()
        ScoreChart(
            theme = state.theme,
            dateFormatter = JavaLocalDateFormatter(Locale.getDefault()),
            scores = state.scores,
            color = state.theme.color(state.color),
            bucketSize = state.bucketSize
        )
    }
    Box(Modifier.fillMaxWidth().testTag("scoreCard")) {
        Column {
            CardTitle(stringResource(R.string.score), color)
            CoreChart(chart, scroll, Modifier.fillMaxWidth().height(220.dp).testTag("scoreChart"))
        }
        CardSpinner(
            entries = stringArrayResource(R.array.strengthIntervalNames).toList(),
            selected = state.spinnerPosition,
            onSelected = onSpinner,
            color = color,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
fun FrequencyCardContent(state: FrequencyCardState) {
    val color = Color(state.theme.color(state.color).toInt())
    val scroll = remember { ChartScrollState() }
    val chart = remember(state) {
        FrequencyChart(
            theme = state.theme,
            dateFormatter = JavaLocalDateFormatter(Locale.getDefault()),
            frequency = state.frequency,
            color = state.theme.color(state.color),
            isNumerical = state.isNumerical,
            firstWeekday = state.firstWeekday
        )
    }
    Column(Modifier.fillMaxWidth().testTag("frequencyCard")) {
        CardTitle(stringResource(R.string.frequency), color)
        CoreChart(chart, scroll, Modifier.fillMaxWidth().height(200.dp).testTag("frequencyChart"))
    }
}
