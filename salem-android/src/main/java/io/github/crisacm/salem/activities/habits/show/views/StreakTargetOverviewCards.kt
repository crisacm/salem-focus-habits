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

import android.content.res.Resources
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.habits.list.ScoreRing
import io.github.crisacm.salem.activities.habits.list.toShortString
import io.github.crisacm.salem.core.ui.screens.habits.show.views.OverviewCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.StreakCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.TargetCardState
import io.github.crisacm.salem.core.ui.views.StreakChart
import io.github.crisacm.salem.core.ui.views.TargetChart
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.ui.compose.CoreView
import java.util.Locale
import kotlin.math.abs

/** The label of the interval of a target row; the target widget uses it too. */
fun intervalToLabel(resources: Resources, interval: Int): String = when (interval) {
    1 -> resources.getString(R.string.today)
    7 -> resources.getString(R.string.week)
    30 -> resources.getString(R.string.month)
    91 -> resources.getString(R.string.quarter)
    else -> resources.getString(R.string.year)
}

/** A change of the score as a signed percentage: "+23%" or "\u221223%". */
fun formatPercentageDiff(percentageDiff: Float): String =
    String.format("%s%.0f%%", if (percentageDiff >= 0) "+" else "\u2212", abs(percentageDiff) * 100)

@Composable
fun StreakCardContent(state: StreakCardState) {
    val color = Color(state.theme.color(state.color).toInt())
    val chart = remember(state) {
        val formatter = JavaLocalDateFormatter(Locale.getDefault())
        StreakChart(state.theme, formatter::longFormat, state.bestStreaks, state.theme.color(state.color))
    }
    Column(Modifier.fillMaxWidth().testTag("streakCard")) {
        CardTitle(stringResource(R.string.best_streaks), color)
        CoreView(chart, Modifier.fillMaxWidth().height(chart.height.dp).testTag("streakChart"))
    }
}

@Composable
fun TargetCardContent(state: TargetCardState) {
    val color = Color(state.theme.color(state.color).toInt())
    val resources = LocalContext.current.resources
    val chart = remember(state) {
        TargetChart(
            theme = state.theme,
            labels = state.intervals.map { intervalToLabel(resources, it) },
            values = state.values,
            targets = state.targets,
            color = state.theme.color(state.color),
            formatValue = { it.toShortString() }
        )
    }
    Column(Modifier.fillMaxWidth().testTag("targetCard")) {
        CardTitle(stringResource(R.string.target), color)
        CoreView(chart, Modifier.fillMaxWidth().height(chart.height.dp).testTag("targetChart"))
    }
}

@Composable
fun OverviewCardContent(state: OverviewCardState) {
    val color = Color(state.theme.color(state.color).toInt())
    val muted = Color(state.theme.mediumContrastTextColor.toInt())
    Column(Modifier.fillMaxWidth().testTag("overviewCard")) {
        CardTitle(stringResource(R.string.overview), color)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(5f), contentAlignment = Alignment.Center) {
                ScoreRing(
                    percentage = state.scoreToday,
                    color = color,
                    inactiveColor = Color(state.theme.highContrastTextColor.withAlpha(0.15).toInt()),
                    diameter = 30.dp,
                    thickness = 5.dp,
                    precision = 0.01f
                )
            }
            OverviewValue("%.0f%%".format(state.scoreToday * 100), color, R.string.score, muted, Modifier.weight(4f))
            OverviewValue(
                formatPercentageDiff(state.scoreMonthDiff),
                if (state.scoreMonthDiff >= 0) color else muted,
                R.string.month,
                muted,
                Modifier.weight(4f)
            )
            OverviewValue(
                formatPercentageDiff(state.scoreYearDiff),
                if (state.scoreYearDiff >= 0) color else muted,
                R.string.year,
                muted,
                Modifier.weight(4f)
            )
            OverviewValue(state.totalCount.toString(), color, R.string.total, muted, Modifier.weight(4f))
        }
    }
}

@Composable
private fun OverviewValue(value: String, valueColor: Color, label: Int, labelColor: Color, modifier: Modifier) {
    Column(modifier) {
        Text(value, color = valueColor, fontSize = 14.sp)
        Text(stringResource(label), color = labelColor, fontSize = 14.sp)
    }
}
