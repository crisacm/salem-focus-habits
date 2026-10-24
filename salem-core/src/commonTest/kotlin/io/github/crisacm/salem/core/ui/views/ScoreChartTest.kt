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
import io.github.crisacm.salem.core.ui.screens.habits.show.views.ScoreCardPresenter.Companion.buildState
import io.github.crisacm.salem.platform.gui.assertRenders
import io.github.crisacm.salem.platform.io.createTestDateFormatter
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ScoreChartTest : BaseUnitTest() {
    private val base = "views/ScoreChart"

    private fun chartFor(spinnerPosition: Int = 0, theme: Theme = LightTheme()): ScoreChart {
        val habit = fixtures.createLongHabit()
        val state = buildState(habit = habit, firstWeekday = 7, spinnerPosition = spinnerPosition, theme = theme)
        return ScoreChart(theme, createTestDateFormatter(), state.scores, theme.color(state.color), state.bucketSize)
    }

    @Test
    fun testDraw() = runTest {
        assertRenders(300, 200, "$base/base.png", chartFor())
    }

    @Test
    fun testDrawWithOffset() = runTest {
        val chart = chartFor().apply { dataOffset = 6 }
        assertRenders(300, 200, "$base/offset.png", chart)
    }

    @Test
    fun testDrawDifferentSize() = runTest {
        assertRenders(200, 200, "$base/small.png", chartFor())
    }

    @Test
    fun testDrawMonthly() = runTest {
        assertRenders(300, 200, "$base/monthly.png", chartFor(spinnerPosition = 2))
    }

    @Test
    fun testDrawYearly() = runTest {
        assertRenders(300, 200, "$base/yearly.png", chartFor(spinnerPosition = 4))
    }

    @Test
    fun testDrawDarkTheme() = runTest {
        assertRenders(300, 200, "$base/themeDark.png", chartFor(theme = DarkTheme()))
    }

    @Test
    fun testDrawWidgetTheme() = runTest {
        assertRenders(300, 200, "$base/themeWidget.png", chartFor(theme = WidgetTheme()))
    }
}
