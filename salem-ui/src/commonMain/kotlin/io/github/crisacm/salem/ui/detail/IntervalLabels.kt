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

import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.day
import io.github.crisacm.salem.ui.resources.month
import io.github.crisacm.salem.ui.resources.quarter
import io.github.crisacm.salem.ui.resources.today
import io.github.crisacm.salem.ui.resources.week
import io.github.crisacm.salem.ui.resources.year
import org.jetbrains.compose.resources.StringResource

/**
 * The names of the intervals of the score and bar cards, in the order of the spinner positions of the core:
 * day, week, month, quarter, year, or the same without the day (a yes or no habit has no daily bars).
 */
fun intervalNames(withDay: Boolean): List<StringResource> {
    val names = listOf(Res.string.week, Res.string.month, Res.string.quarter, Res.string.year)
    return if (withDay) listOf(Res.string.day) + names else names
}

/** The label of the interval, in days, of a row of the target card. */
fun intervalLabelRes(interval: Int): StringResource = when (interval) {
    1 -> Res.string.today
    7 -> Res.string.week
    30 -> Res.string.month
    91 -> Res.string.quarter
    else -> Res.string.year
}
