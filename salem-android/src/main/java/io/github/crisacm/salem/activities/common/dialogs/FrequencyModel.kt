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

package io.github.crisacm.salem.activities.common.dialogs

enum class FrequencyMode { EVERY_DAY, EVERY_X_DAYS, X_TIMES_PER_WEEK, X_TIMES_PER_MONTH, X_TIMES_IN_Y_DAYS }

/** What the user typed in the frequency picker. The fields of the modes that are not selected keep their defaults. */
data class FrequencyInput(
    val mode: FrequencyMode,
    val everyXDays: String = DEFAULT_EVERY_X_DAYS,
    val perWeek: String = DEFAULT_PER_WEEK,
    val perMonth: String = DEFAULT_PER_MONTH,
    val x: String = DEFAULT_X,
    val y: String = DEFAULT_Y
)

private const val DEFAULT_EVERY_X_DAYS = "3"
private const val DEFAULT_PER_WEEK = "3"
private const val DEFAULT_PER_MONTH = "10"
private const val DEFAULT_X = "3"
private const val DEFAULT_Y = "14"

/**
 * The (numerator, denominator) saved for [input]. Empty or invalid numbers, a numerator that is not
 * lower than the denominator, and a numerator below 1 all give every day (1/1).
 */
fun frequencyFromInput(input: FrequencyInput): Pair<Int, Int> {
    var numerator = 1
    var denominator = 1
    when (input.mode) {
        FrequencyMode.EVERY_DAY -> {}
        FrequencyMode.EVERY_X_DAYS -> input.everyXDays.toIntOrNull()?.let { denominator = it }
        FrequencyMode.X_TIMES_PER_WEEK -> input.perWeek.toIntOrNull()?.let {
            numerator = it
            denominator = 7
        }
        FrequencyMode.X_TIMES_PER_MONTH -> input.perMonth.toIntOrNull()?.let {
            numerator = it
            denominator = 30
        }
        FrequencyMode.X_TIMES_IN_Y_DAYS -> {
            val times = input.x.toIntOrNull()
            val days = input.y.toIntOrNull()
            if (times != null && days != null) {
                numerator = times
                denominator = days
            }
        }
    }
    return if (numerator >= denominator || numerator < 1) 1 to 1 else numerator to denominator
}

/** The picker state for a stored frequency: the matching mode, with its fields filled. */
fun initialFrequencyInput(numerator: Int, denominator: Int): FrequencyInput = when {
    denominator == 30 || denominator == 31 ->
        FrequencyInput(FrequencyMode.X_TIMES_PER_MONTH, perMonth = numerator.toString())
    numerator == 1 && denominator == 1 -> FrequencyInput(FrequencyMode.EVERY_DAY)
    numerator == 1 -> FrequencyInput(FrequencyMode.EVERY_X_DAYS, everyXDays = denominator.toString())
    denominator == 7 -> FrequencyInput(FrequencyMode.X_TIMES_PER_WEEK, perWeek = numerator.toString())
    else -> FrequencyInput(FrequencyMode.X_TIMES_IN_Y_DAYS, x = numerator.toString(), y = denominator.toString())
}

sealed class FrequencyLabel {
    data object EveryMonth : FrequencyLabel()
    data class TimesPerMonth(val times: Int) : FrequencyLabel()
    data object EveryDay : FrequencyLabel()
    data object EveryWeek : FrequencyLabel()
    data class EveryXDays(val days: Int) : FrequencyLabel()
    data class TimesPerWeek(val times: Int) : FrequencyLabel()
    data class TimesInDays(val times: Int, val days: Int) : FrequencyLabel()
}

fun frequencyLabel(numerator: Int, denominator: Int): FrequencyLabel = when {
    numerator == 1 && (denominator == 30 || denominator == 31) -> FrequencyLabel.EveryMonth
    denominator == 30 || denominator == 31 -> FrequencyLabel.TimesPerMonth(numerator)
    numerator == 1 && denominator == 1 -> FrequencyLabel.EveryDay
    numerator == 1 && denominator == 7 -> FrequencyLabel.EveryWeek
    numerator == 1 && denominator > 1 -> FrequencyLabel.EveryXDays(denominator)
    denominator == 7 -> FrequencyLabel.TimesPerWeek(numerator)
    else -> FrequencyLabel.TimesInDays(numerator, denominator)
}
