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
import io.github.crisacm.salem.platform.time.LocalDate
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class StreakChartTest : BaseUnitTest() {
    private val base = "views/StreakChart"

    private fun chart(count: Int = 5, theme: Theme = LightTheme(), width: Int = 300): StreakChart {
        val habit = fixtures.createLongHabit()
        val formatter = createTestDateFormatter()
        return StreakChart(
            theme = theme,
            formatDate = { date: LocalDate -> "${formatter.shortMonthName(date)} ${date.day}, ${date.year}" },
            streaks = habit.streaks.getBest(count),
            color = theme.color(habit.color)
        )
    }

    @Test
    fun testDraw() = runTest {
        val chart = chart()
        assertRenders(300, chart.height.toInt(), "$base/base.png", chart)
    }

    @Test
    fun testDrawSmallWidth() = runTest {
        val chart = chart()
        assertRenders(100, chart.height.toInt(), "$base/small.png", chart)
    }

    @Test
    fun testDrawDarkTheme() = runTest {
        val chart = chart(theme = DarkTheme())
        assertRenders(300, chart.height.toInt(), "$base/themeDark.png", chart)
    }

    @Test
    fun testDrawOneStreak() = runTest {
        val chart = chart(count = 1)
        assertRenders(300, chart.height.toInt(), "$base/one.png", chart)
    }
}
