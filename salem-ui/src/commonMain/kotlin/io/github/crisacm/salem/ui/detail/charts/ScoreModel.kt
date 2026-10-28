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

package io.github.crisacm.salem.ui.detail.charts

import io.github.crisacm.salem.core.models.Score
import io.github.crisacm.salem.platform.time.LocalDate
import kotlin.math.roundToInt

/** A point of the score line: the score, from 0 to 1, of the bucket that starts at [date]. */
class ScorePoint(val date: LocalDate, val value: Float)

/** The points of the line, newest first as the card state has them. */
fun scorePoints(scores: List<Score>): List<ScorePoint> =
    scores.map { ScorePoint(it.date, it.value.toFloat().coerceIn(0f, 1f)) }

/** The lines of the grid, from 20% to 100%. */
val ScoreTicks = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1f)

fun percentText(value: Float): String = "${(value * 100).roundToInt()}%"

/** The name under a column of the score: the month for buckets of a month or less, the quarter or the year for longer ones. */
fun scoreAxisLabel(bucketSize: Int, date: LocalDate, monthName: (LocalDate) -> String): String =
    if (bucketSize >= 92) barAxisLabel(bucketSize, date, monthName) else monthName(date)

/**
 * The texts under a chart of the score for buckets of a month or less, which are in fixed places 40 dp apart and
 * not under a column: the short name of the month for the first one and wherever the month changes, the day of the
 * month for the others. [dates] are the dates of the columns that are under those places, from the oldest.
 */
fun scoreAxisTexts(dates: List<LocalDate>, monthName: (LocalDate) -> String): List<String> =
    dates.mapIndexed { index, date ->
        if (index == 0 || dates[index - 1].month != date.month || dates[index - 1].year != date.year) {
            monthName(date)
        } else {
            date.day.toString()
        }
    }

/** The distance between two texts under a chart of the score. */
const val SCORE_AXIS_SPACING_DP = 40.3f
