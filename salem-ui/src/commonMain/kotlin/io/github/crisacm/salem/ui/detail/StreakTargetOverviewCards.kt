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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.core.ui.screens.habits.show.views.OverviewCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.StreakCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.TargetCardState
import io.github.crisacm.salem.core.ui.views.toShortString
import io.github.crisacm.salem.platform.io.formatInvariant
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.compose.usesDarkTones
import io.github.crisacm.salem.ui.detail.charts.StreakBarsChart
import io.github.crisacm.salem.ui.detail.charts.TargetBarsChart
import io.github.crisacm.salem.ui.detail.charts.TargetRow
import io.github.crisacm.salem.ui.detail.charts.streakBars
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

/** A change of the score as a signed percentage: "+23%" or "\u221223%". */
fun formatPercentageDiff(percentageDiff: Float): String =
    (if (percentageDiff >= 0) "+" else "\u2212") + formatInvariant("%.0f", abs(percentageDiff) * 100.0) + "%"

@Composable
fun StreakCardContent(state: StreakCardState, dateFormatter: LocalDateFormatter) {
    val tones = rememberDetailTones(state.theme, state.color)
    val bars = remember(state) { streakBars(state.bestStreaks) }
    Column(Modifier.fillMaxWidth().testTag("streakCard")) {
        CardHeader(stringResource(Res.string.best_streaks), spacing = 12.dp)
        StreakBarsChart(bars, tones, dateFormatter::longFormat, Modifier.testTag("streakChart"))
    }
}

@Composable
fun TargetCardContent(state: TargetCardState) {
    val tones = rememberDetailTones(state.theme, state.color)
    val labels = state.intervals.map { stringResource(intervalLabelRes(it)) }
    val rows = remember(state, labels) {
        labels.mapIndexed { i, label -> TargetRow(label, state.values[i], state.targets[i]) }
    }
    Column(Modifier.fillMaxWidth().testTag("targetCard")) {
        CardHeader(stringResource(Res.string.target), spacing = 12.dp)
        TargetBarsChart(rows, tones, { it.toShortString() }, Modifier.testTag("targetChart"))
    }
}

/**
 * The overview of a yes or no habit: the score in a large card in the colour of the habit with a ring, and under it the
 * change of the score in the last month and year and the total of completions. It has its own margins, as it is not in a card.
 */
@Composable
fun OverviewCardContent(state: OverviewCardState) {
    val tones = rememberDetailTones(state.theme, state.color)
    val scheme = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("overviewCard"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            stringResource(Res.string.overview),
            color = scheme.onSurface,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp, top = 7.dp, bottom = 4.dp)
        )
        Surface(color = tones.accent, shape = RoundedCornerShape(36.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 24.dp, end = 28.dp, top = 23.dp, bottom = 23.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Res.string.score), color = tones.onAccent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        formatInvariant("%.0f", state.scoreToday * 100.0) + "%",
                        color = tones.onAccent,
                        fontSize = 64.sp,
                        lineHeight = 68.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-2).sp
                    )
                }
                ScoreDonut(state.scoreToday, tones.onAccent, if (state.theme.usesDarkTones()) 0.36f else 0.30f)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(formatPercentageDiff(state.scoreMonthDiff), Res.string.month, scheme.surfaceContainer, tones.emphasis, scheme.onSurfaceVariant)
            StatCard(formatPercentageDiff(state.scoreYearDiff), Res.string.year, scheme.surfaceContainer, tones.emphasis, scheme.onSurfaceVariant)
            StatCard(state.totalCount.toString(), Res.string.total, tones.container, tones.onContainer, tones.onContainer)
        }
    }
}

@Composable
private fun RowScope.StatCard(value: String, label: StringResource, container: Color, valueColor: Color, labelColor: Color) {
    Surface(color = container, shape = RoundedCornerShape(28.dp), modifier = Modifier.weight(1f)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = valueColor, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(stringResource(label), color = labelColor, fontSize = 16.sp, lineHeight = 20.sp)
        }
    }
}

/** The ring of the score of the overview: a track and an arc from the top, as thick as the large number is bold. */
@Composable
private fun ScoreDonut(score: Float, color: Color, trackAlpha: Float) {
    Canvas(Modifier.size(88.dp)) {
        val stroke = 12.dp.toPx()
        val topLeft = Offset(stroke / 2, stroke / 2)
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(color.copy(alpha = trackAlpha), -90f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
        drawArc(color, -90f, 360f * score.coerceIn(0f, 1f), useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
    }
}
