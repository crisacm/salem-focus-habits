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

import kotlin.math.max
import kotlin.math.min

/** The part of the track that the progress of a target fills; a target of zero or less counts as reached. */
fun targetFraction(value: Double, target: Double): Float =
    if (target <= 0.0) 1f else min(1.0, max(0.0, value / target)).toFloat()

/** What is left to reach the target, never below zero. */
fun targetRemaining(value: Double, target: Double): Double = max(0.0, target - value)

/** Whether the fill reaches the middle of the track, where the number is, so the number takes the colour for the fill. */
fun targetTextOnFill(fraction: Float): Boolean = fraction >= 0.5f

const val TARGET_TRACK_HEIGHT_DP = 44f
const val TARGET_ROW_GAP_DP = 8f
const val TARGET_FILL_INSET_DP = 1.5f
const val TARGET_FILL_MIN_WIDTH_DP = 41f
const val TARGET_LABEL_WIDTH_DP = 71.7f
