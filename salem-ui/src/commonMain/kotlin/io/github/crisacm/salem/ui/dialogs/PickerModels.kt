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

package io.github.crisacm.salem.ui.dialogs

import androidx.compose.ui.graphics.Color
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.platform.time.getWeekdaySequence
import io.github.crisacm.salem.ui.compose.toCompose

private const val PALETTE_SIZE = 20

/** The colours a habit can have in this theme, by palette index. */
fun Theme.paletteColors(): List<Color> = (0 until PALETTE_SIZE).map { color(it).toCompose() }

/** Long weekday names starting at Saturday, the order of a `WeekdayList`. */
fun weekdayNames(formatter: LocalDateFormatter): List<String> =
    getWeekdaySequence(DayOfWeek.SATURDAY).map(formatter::longWeekdayName)
