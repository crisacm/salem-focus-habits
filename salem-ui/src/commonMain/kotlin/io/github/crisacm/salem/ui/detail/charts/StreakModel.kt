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

import io.github.crisacm.salem.core.models.Streak
import io.github.crisacm.salem.platform.time.LocalDate
import kotlin.math.max

/** How long a streak is next to the longest one: the full colour, the muted one or the dim one. */
enum class StreakTier { TOP, MID, LOW }

/** One pill of the best streaks: [fraction] is its length over the longest one. */
class StreakBar(val start: LocalDate, val end: LocalDate, val length: Int, val fraction: Float, val tier: StreakTier)

/** The pills of [streaks], in the order they come in, with the length of each one relative to the longest. */
fun streakBars(streaks: List<Streak>): List<StreakBar> {
    val longest = streaks.maxOfOrNull { it.length } ?: return emptyList()
    return streaks.map {
        val fraction = if (longest <= 0) 0f else it.length.toFloat() / longest
        StreakBar(it.start, it.end, it.length, fraction, streakTier(fraction))
    }
}

fun streakTier(fraction: Float): StreakTier = when {
    fraction >= 1f -> StreakTier.TOP
    fraction >= 0.5f -> StreakTier.MID
    else -> StreakTier.LOW
}

/** The width of a pill: its share of [longestWidth], never narrower than [STREAK_MIN_WIDTH_DP] so the number fits. */
fun streakPillWidth(fraction: Float, longestWidth: Float): Float = max(fraction * longestWidth, STREAK_MIN_WIDTH_DP)

/** The dates fit at both sides of a pill when each side has at least [STREAK_DATE_ROOM_DP] left. */
fun streakDatesFit(sideRoom: Float): Boolean = sideRoom >= STREAK_DATE_ROOM_DP

const val STREAK_PILL_HEIGHT_DP = 36f
const val STREAK_ROW_GAP_DP = 8f
const val STREAK_LONGEST_WIDTH_DP = 147f
const val STREAK_MIN_WIDTH_DP = 41f
const val STREAK_DATE_ROOM_DP = 64f
