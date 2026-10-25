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

package io.github.crisacm.salem.ui.list

import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/** The width that the name of a habit has at least, as `habitNameWidth` in the Android resources. */
val HabitNameWidth = 160.dp

/**
 * How many day columns fit in [width] pixels: the name of the habit takes a third of the width, or
 * [nameWidth] if that is larger, and each column is [buttonWidth] wide.
 */
fun checkmarkColumns(width: Float, nameWidth: Float, buttonWidth: Float): Int {
    val labelWidth = max(width / 3, nameWidth)
    val buttonCount = ((width - labelWidth) / buttonWidth).toInt()
    return min(MAX_CHECKMARK_COUNT, max(0, buttonCount))
}
