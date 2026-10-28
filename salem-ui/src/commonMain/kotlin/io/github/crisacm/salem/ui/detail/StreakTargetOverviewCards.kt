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
import androidx.compose.foundation.layout.height
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
import io.github.crisacm.salem.core.ui.views.StreakChart
import io.github.crisacm.salem.core.ui.views.TargetChart
import io.github.crisacm.salem.core.ui.views.toShortString
import io.github.crisacm.salem.platform.io.formatInvariant
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.canvas.CoreCanvasView
import io.github.crisacm.salem.ui.canvas.rememberSalemFonts
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
    val fonts = rememberSalemFonts()
    val chart = remember(state) {
        StreakChart(state.theme, dateFormatter::longFormat, state.bestStreaks, state.theme.color(state.color))
    }
    Column(Modifier.fillMaxWidth().testTag("streakCard")) {
        CardHeader(stringResource(Res.string.best_streaks))
        CoreCanvasView(chart, fonts, Modifier.fillMaxWidth().height(chart.height.dp).testTag("streakChart"))
    }
}

@Composable
fun TargetCardContent(state: TargetCardState) {
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
        CardHeader(stringResource(Res.string.target))
        CoreCanvasView(chart, fonts, Modifier.fillMaxWidth().height(chart.height.dp).testTag("targetChart"))
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            stringResource(Res.string.overview),
            color = scheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp)
        )
        Surface(color = tones.accent, shape = RoundedCornerShape(32.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 24.dp, end = 28.dp, top = 20.dp, bottom = 20.dp)
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
                ScoreDonut(state.scoreToday, tones.onAccent)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(formatPercentageDiff(state.scoreMonthDiff), Res.string.month, scheme.surfaceContainer, scheme.onSurface, scheme.onSurfaceVariant)
            StatCard(formatPercentageDiff(state.scoreYearDiff), Res.string.year, scheme.surfaceContainer, scheme.onSurface, scheme.onSurfaceVariant)
            StatCard(state.totalCount.toString(), Res.string.total, tones.container, tones.onContainer, tones.onContainer)
        }
    }
}

@Composable
private fun RowScope.StatCard(value: String, label: StringResource, container: Color, valueColor: Color, labelColor: Color) {
    Surface(color = container, shape = RoundedCornerShape(24.dp), modifier = Modifier.weight(1f)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(value, color = valueColor, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(stringResource(label), color = labelColor, fontSize = 14.sp)
        }
    }
}

/** The ring of the score of the overview: a track and an arc from the top, as thick as the large number is bold. */
@Composable
private fun ScoreDonut(score: Float, color: Color) {
    Canvas(Modifier.size(88.dp)) {
        val stroke = 14.dp.toPx()
        val topLeft = Offset(stroke / 2, stroke / 2)
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(color.copy(alpha = 0.3f), -90f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
        drawArc(color, -90f, 360f * score.coerceIn(0f, 1f), useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
    }
}
