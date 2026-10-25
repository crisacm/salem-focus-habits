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
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.compose.toCompose
import org.junit.Test
import kotlin.test.assertEquals

class PickerModelsTest {
    @Test
    fun theLightPaletteHasTwentyColorsOfTheTheme() {
        val palette = LightTheme().paletteColors()
        assertEquals(20, palette.size)
        assertEquals(Color(0xFFD32F2F), palette[0])
        assertEquals(Color(0xFF9E9E9E), palette[19])
    }

    @Test
    fun theDarkPaletteFollowsTheDarkTheme() {
        val theme = DarkTheme()
        val palette = theme.paletteColors()
        assertEquals(20, palette.size)
        assertEquals((0 until 20).map { theme.color(it).toCompose() }, palette)
        assertEquals(Color(0xFFEF9A9A), palette[0])
    }

    @Test
    fun weekdayNamesStartAtSaturdayLikeTheWeekdayList() {
        val names = weekdayNames(FakeFormatter())
        assertEquals(
            listOf("long SATURDAY", "long SUNDAY", "long MONDAY", "long TUESDAY", "long WEDNESDAY", "long THURSDAY", "long FRIDAY"),
            names
        )
    }

    private class FakeFormatter : LocalDateFormatter {
        override fun narrowWeekdayName(weekday: DayOfWeek) = "narrow $weekday"
        override fun narrowWeekdayName(date: LocalDate) = "narrow"
        override fun shortWeekdayName(weekday: DayOfWeek) = "short $weekday"
        override fun shortWeekdayName(date: LocalDate) = "short"
        override fun shortMonthName(date: LocalDate) = "month"
        override fun longWeekdayName(weekday: DayOfWeek) = "long $weekday"
        override fun longMonthName(date: LocalDate) = "long month"
        override fun longFormat(date: LocalDate) = "long date"
    }
}
