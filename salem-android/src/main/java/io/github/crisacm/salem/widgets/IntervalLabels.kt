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

package io.github.crisacm.salem.widgets

import android.content.res.Resources
import io.github.crisacm.salem.R

/** The label of the interval, in days, of a row of the target widget. */
fun intervalToLabel(resources: Resources, interval: Int): String = when (interval) {
    1 -> resources.getString(R.string.today)
    7 -> resources.getString(R.string.week)
    30 -> resources.getString(R.string.month)
    91 -> resources.getString(R.string.quarter)
    else -> resources.getString(R.string.year)
}
