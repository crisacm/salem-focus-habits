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

import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.ui.views.toShortString
import io.github.crisacm.salem.platform.time.LocalDate
import kotlin.math.max

/** A bar of the history: the sum of the entries of the bucket that starts at [date]. */
class BarColumn(val date: LocalDate, val value: Double)

/** The bars, newest first as the card state has them; entries store thousandths. */
fun barColumns(entries: List<Entry>): List<BarColumn> = entries.map { BarColumn(it.date, it.value / 1000.0) }

/** The value that fills the whole height of the chart; a chart with nothing in it still has a scale. */
fun barScaleMax(values: List<Double>): Double = max(values.maxOrNull() ?: 0.0, 1.0)

/** The number over a bar: a whole number as it is, anything else in the short form of the numbers. */
fun barValueText(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toShortString()

/** The label under a bar for a bucket of [bucketSize] days; [monthName] gives the short name of a month. */
fun barAxisLabel(bucketSize: Int, date: LocalDate, monthName: (LocalDate) -> String): String = when (bucketSize) {
    1, 7 -> date.day.toString()
    31 -> monthName(date).take(1).uppercase()
    92 -> "Q${(date.month - 1) / 3 + 1}"
    else -> date.year.toString()
}
