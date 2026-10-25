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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.ui.screens.habits.show.views.OverviewCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.StreakCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.TargetCardState
import io.github.crisacm.salem.core.ui.views.StreakChart
import io.github.crisacm.salem.core.ui.views.TargetChart
import io.github.crisacm.salem.core.ui.views.toShortString
import io.github.crisacm.salem.platform.io.formatInvariant
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.canvas.CoreCanvasView
import io.github.crisacm.salem.ui.canvas.rememberSalemFonts
import io.github.crisacm.salem.ui.compose.toCompose
import io.github.crisacm.salem.ui.list.ScoreRing
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.best_streaks
import io.github.crisacm.salem.ui.resources.month
import io.github.crisacm.salem.ui.resources.overview
import io.github.crisacm.salem.ui.resources.score
import io.github.crisacm.salem.ui.resources.target
import io.github.crisacm.salem.ui.resources.total
import io.github.crisacm.salem.ui.resources.year
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

/** A change of the score as a signed percentage: "+23%" or "−23%". */
fun formatPercentageDiff(percentageDiff: Float): String =
    (if (percentageDiff >= 0) "+" else "−") + formatInvariant("%.0f", abs(percentageDiff) * 100.0) + "%"

@Composable
fun StreakCardContent(state: StreakCardState, dateFormatter: LocalDateFormatter) {
    val color = state.theme.color(state.color).toCompose()
    val fonts = rememberSalemFonts()
    val chart = remember(state) {
        StreakChart(state.theme, dateFormatter::longFormat, state.bestStreaks, state.theme.color(state.color))
    }
    Column(Modifier.fillMaxWidth().testTag("streakCard")) {
        CardTitle(stringResource(Res.string.best_streaks), color)
        CoreCanvasView(chart, fonts, Modifier.fillMaxWidth().height(chart.height.dp).testTag("streakChart"))
    }
}

@Composable
fun TargetCardContent(state: TargetCardState) {
    val color = state.theme.color(state.color).toCompose()
    val fonts = rememberSalemFonts()
    val labels = state.intervals.map { stringResource(intervalLabelRes(it)) }
    val chart = remember(state, labels) {
        TargetChart(
            theme = state.theme,
            labels = labels,
            values = state.values,
            targets = state.targets,
            color = state.theme.color(state.color),
            formatValue = { it.toShortString() }
        )
    }
    Column(Modifier.fillMaxWidth().testTag("targetCard")) {
        CardTitle(stringResource(Res.string.target), color)
        CoreCanvasView(chart, fonts, Modifier.fillMaxWidth().height(chart.height.dp).testTag("targetChart"))
    }
}

@Composable
fun OverviewCardContent(state: OverviewCardState) {
    val color = state.theme.color(state.color).toCompose()
    val muted = state.theme.mediumContrastTextColor.toCompose()
    Column(Modifier.fillMaxWidth().testTag("overviewCard")) {
        CardTitle(stringResource(Res.string.overview), color)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(5f), contentAlignment = Alignment.Center) {
                ScoreRing(
                    percentage = state.scoreToday,
                    color = color,
                    inactiveColor = state.theme.highContrastTextColor.withAlpha(0.15).toCompose(),
                    diameter = 30.dp,
                    thickness = 5.dp,
                    precision = 0.01f
                )
            }
            OverviewValue(
                formatInvariant("%.0f", state.scoreToday * 100.0) + "%",
                color,
                Res.string.score,
                muted,
                Modifier.weight(4f)
            )
            OverviewValue(
                formatPercentageDiff(state.scoreMonthDiff),
                if (state.scoreMonthDiff >= 0) color else muted,
                Res.string.month,
                muted,
                Modifier.weight(4f)
            )
            OverviewValue(
                formatPercentageDiff(state.scoreYearDiff),
                if (state.scoreYearDiff >= 0) color else muted,
                Res.string.year,
                muted,
                Modifier.weight(4f)
            )
            OverviewValue(state.totalCount.toString(), color, Res.string.total, muted, Modifier.weight(4f))
        }
    }
}

@Composable
private fun OverviewValue(value: String, valueColor: Color, label: StringResource, labelColor: Color, modifier: Modifier) {
    Column(modifier) {
        Text(value, color = valueColor, fontSize = SMALL_TEXT)
        Text(stringResource(label), color = labelColor, fontSize = SMALL_TEXT)
    }
}
