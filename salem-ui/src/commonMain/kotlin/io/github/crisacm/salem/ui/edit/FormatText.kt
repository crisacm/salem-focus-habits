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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.platform.time.getWeekdaySequence
import io.github.crisacm.salem.ui.dialogs.FrequencyLabel
import io.github.crisacm.salem.ui.dialogs.frequencyLabel
import io.github.crisacm.salem.ui.dialogs.weekdayNames
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.any_day
import io.github.crisacm.salem.ui.resources.any_weekday
import io.github.crisacm.salem.ui.resources.every_day
import io.github.crisacm.salem.ui.resources.every_month
import io.github.crisacm.salem.ui.resources.every_week
import io.github.crisacm.salem.ui.resources.every_x_days
import io.github.crisacm.salem.ui.resources.weekends
import io.github.crisacm.salem.ui.resources.x_times_per_month
import io.github.crisacm.salem.ui.resources.x_times_per_week
import io.github.crisacm.salem.ui.resources.x_times_per_y_days
import org.jetbrains.compose.resources.stringResource

/** "3 times per week", "Every 2 days"... for a stored frequency. */
@Composable
fun frequencyText(numerator: Int, denominator: Int): String = when (val label = frequencyLabel(numerator, denominator)) {
    FrequencyLabel.EveryMonth -> stringResource(Res.string.every_month)
    is FrequencyLabel.TimesPerMonth -> stringResource(Res.string.x_times_per_month, label.times)
    FrequencyLabel.EveryDay -> stringResource(Res.string.every_day)
    FrequencyLabel.EveryWeek -> stringResource(Res.string.every_week)
    is FrequencyLabel.EveryXDays -> stringResource(Res.string.every_x_days, label.days)
    is FrequencyLabel.TimesPerWeek -> stringResource(Res.string.x_times_per_week, label.times)
    is FrequencyLabel.TimesInDays -> stringResource(Res.string.x_times_per_y_days, label.times, label.days)
}

/** The frequency of a numerical habit, which only has the options of its list; anything else shows `n/d`. */
@Composable
fun numericalFrequencyText(numerator: Int, denominator: Int): String = when (numericalFrequencyKind(denominator)) {
    NumericalFrequencyKind.EVERY_DAY -> stringResource(Res.string.every_day)
    NumericalFrequencyKind.EVERY_WEEK -> stringResource(Res.string.every_week)
    NumericalFrequencyKind.EVERY_MONTH -> stringResource(Res.string.every_month)
    NumericalFrequencyKind.OTHER -> "$numerator/$denominator"
}

/** The days of a reminder as text: "Weekends", "Mon, Wed"... */
@Composable
fun weekdaysText(days: WeekdayList, formatter: LocalDateFormatter): String {
    val short = remember(formatter) { getWeekdaySequence(DayOfWeek.SATURDAY).map(formatter::shortWeekdayName) }
    val long = remember(formatter) { weekdayNames(formatter) }
    return when (val summary = weekdaysSummary(days, short, long)) {
        is WeekdaysSummary.Single -> summary.name
        WeekdaysSummary.Weekends -> stringResource(Res.string.weekends)
        WeekdaysSummary.AnyWeekday -> stringResource(Res.string.any_weekday)
        WeekdaysSummary.AnyDay -> stringResource(Res.string.any_day)
        is WeekdaysSummary.Custom -> summary.text
    }
}
