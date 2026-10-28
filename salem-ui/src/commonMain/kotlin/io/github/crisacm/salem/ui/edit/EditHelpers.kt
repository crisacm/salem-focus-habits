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

package io.github.crisacm.salem.ui.edit

import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.getWeekdaySequence

/** How the days of a reminder are summed up; [Custom] lists the short names of the days. */
sealed class WeekdaysSummary {
    data class Single(val name: String) : WeekdaysSummary()
    data object Weekends : WeekdaysSummary()
    data object AnyWeekday : WeekdaysSummary()
    data object AnyDay : WeekdaysSummary()
    data class Custom(val text: String) : WeekdaysSummary()
}

/** [shortNames] and [longNames] start at Saturday, as a [WeekdayList] does. */
fun weekdaysSummary(days: WeekdayList, shortNames: List<String>, longNames: List<String>): WeekdaysSummary {
    val selected = days.toArray()
    val indexes = selected.indices.filter { selected[it] }
    return when {
        indexes.size == 1 -> WeekdaysSummary.Single(longNames[indexes.first()])
        indexes.size == 2 && selected[0] && selected[1] -> WeekdaysSummary.Weekends
        indexes.size == 5 && !selected[0] && !selected[1] -> WeekdaysSummary.AnyWeekday
        indexes.size == 7 -> WeekdaysSummary.AnyDay
        else -> WeekdaysSummary.Custom(indexes.joinToString(", ") { shortNames[it] })
    }
}

/** The denominator of the choice picked in the frequency list of a numerical habit: every day, week or month. */
fun denominatorForChoice(index: Int): Int = when (index) {
    1 -> 7
    2 -> 30
    else -> 1
}

enum class NumericalFrequencyKind { EVERY_DAY, EVERY_WEEK, EVERY_MONTH, OTHER }

fun numericalFrequencyKind(denominator: Int) = when (denominator) {
    1 -> NumericalFrequencyKind.EVERY_DAY
    7 -> NumericalFrequencyKind.EVERY_WEEK
    30 -> NumericalFrequencyKind.EVERY_MONTH
    else -> NumericalFrequencyKind.OTHER
}

private const val DEFAULT_REMINDER_HOUR = 8

/** The time the clock starts at: the reminder of the form, or 08:00 while it has none. */
fun reminderStartTime(hour: Int, minute: Int): Pair<Int, Int> =
    if (hour >= 0) hour to maxOf(minute, 0) else DEFAULT_REMINDER_HOUR to 0

/** Whether a date pattern (such as the one of the template `j`) shows the hours from 0 to 23. */
fun uses24Hour(pattern: String): Boolean {
    val letters = pattern.split('\'').filterIndexed { index, _ -> index % 2 == 0 }.joinToString("")
    return 'H' in letters || 'k' in letters
}

/** The colours the editor offers at a glance: blue, pink, orange and purple (the others are in the picker). */
val QuickColors = listOf(11, 15, 2, 13)

/** The swatches to show for a habit of colour [current]: the quick colours, with a colour that is not one of them in the first place. */
fun swatchColors(current: PaletteColor): List<Int> =
    if (current.paletteIndex in QuickColors) QuickColors else listOf(current.paletteIndex) + QuickColors.drop(1)

/** The chips of the frequency of a habit; [CUSTOM] is anything that the other two do not say. */
enum class FrequencyChoice { EVERY_DAY, PER_WEEK, CUSTOM }

fun frequencyChoice(numerator: Int, denominator: Int, numerical: Boolean): FrequencyChoice = when {
    // A numerical habit only has a period, so its numerator does not tell the chips apart
    numerical && denominator == 1 -> FrequencyChoice.EVERY_DAY
    numerical && denominator == 7 -> FrequencyChoice.PER_WEEK
    !numerical && numerator == 1 && denominator == 1 -> FrequencyChoice.EVERY_DAY
    !numerical && numerator == 1 && denominator == 7 -> FrequencyChoice.PER_WEEK
    else -> FrequencyChoice.CUSTOM
}

/** A day of the week of the reminder, and its position in a [WeekdayList], which starts at Saturday. */
class WeekdaySlot(val day: DayOfWeek, val index: Int)

/** The days of the week in the order they are shown, starting at [firstWeekday], with their place in a [WeekdayList]. */
fun weekdaySlots(firstWeekday: DayOfWeek): List<WeekdaySlot> {
    val listOrder = getWeekdaySequence(DayOfWeek.SATURDAY)
    return getWeekdaySequence(firstWeekday).map { WeekdaySlot(it, listOrder.indexOf(it)) }
}

/** [days] with the day at [index] turned on or off; the last day on stays on, because a reminder needs at least one. */
fun toggleWeekday(days: WeekdayList, index: Int): WeekdayList {
    val selected = days.toArray()
    selected[index] = !selected[index]
    return if (selected.none { it }) days else WeekdayList(selected)
}
