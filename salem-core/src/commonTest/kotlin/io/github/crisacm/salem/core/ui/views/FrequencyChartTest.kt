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

package io.github.crisacm.salem.core.ui.views

import io.github.crisacm.salem.core.BaseUnitTest
import io.github.crisacm.salem.platform.gui.assertRenders
import io.github.crisacm.salem.platform.io.createTestDateFormatter
import io.github.crisacm.salem.platform.time.DayOfWeek
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class FrequencyChartTest : BaseUnitTest() {
    private val base = "views/FrequencyChart"

    private fun chart(theme: Theme = LightTheme(), firstWeekday: DayOfWeek = DayOfWeek.SATURDAY): FrequencyChart {
        val habit = fixtures.createLongHabit()
        return FrequencyChart(
            theme = theme,
            dateFormatter = createTestDateFormatter(),
            frequency = habit.originalEntries.computeWeekdayFrequency(habit.isNumerical),
            color = theme.color(habit.color),
            isNumerical = habit.isNumerical,
            firstWeekday = firstWeekday
        )
    }

    @Test
    fun testDraw() = runTest {
        assertRenders(300, 100, "$base/base.png", chart())
    }

    @Test
    fun testDrawWithOffset() = runTest {
        assertRenders(300, 100, "$base/offset.png", chart().apply { dataOffset = 6 })
    }

    @Test
    fun testDrawDifferentSize() = runTest {
        assertRenders(200, 200, "$base/small.png", chart())
    }

    @Test
    fun testDrawDarkTheme() = runTest {
        assertRenders(300, 100, "$base/themeDark.png", chart(theme = DarkTheme()))
    }

    @Test
    fun testDrawMondayFirst() = runTest {
        assertRenders(300, 100, "$base/monday.png", chart(firstWeekday = DayOfWeek.MONDAY))
    }
}
