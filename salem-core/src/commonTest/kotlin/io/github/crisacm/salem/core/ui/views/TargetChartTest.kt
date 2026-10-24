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

import io.github.crisacm.salem.platform.gui.assertRenders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TargetChartTest {
    private val base = "views/TargetChart"
    private val labels = listOf("Today", "Week", "Month", "Quarter", "Year")

    private fun chart(theme: Theme = LightTheme(), values: List<Double>, targets: List<Double>) = TargetChart(
        theme = theme,
        labels = labels.take(values.size),
        values = values,
        targets = targets,
        color = theme.color(8),
        formatValue = { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }
    )

    @Test
    fun testDraw() = runTest {
        val chart = chart(values = listOf(1.0, 3.0, 12.0, 20.0, 150.0), targets = listOf(1.0, 5.0, 20.0, 60.0, 365.0))
        assertRenders(300, chart.height.toInt(), "$base/base.png", chart)
    }

    @Test
    fun testDrawThreeRows() = runTest {
        val chart = chart(values = listOf(0.0, 4.0, 30.0), targets = listOf(2.0, 5.0, 30.0))
        assertRenders(300, chart.height.toInt(), "$base/three.png", chart)
    }

    @Test
    fun testDrawDarkTheme() = runTest {
        val chart = chart(theme = DarkTheme(), values = listOf(1.0, 3.0, 12.0), targets = listOf(1.0, 5.0, 20.0))
        assertRenders(300, chart.height.toInt(), "$base/themeDark.png", chart)
    }

    @Test
    fun testDrawNarrow() = runTest {
        val chart = chart(values = listOf(1.0, 3.0, 12.0), targets = listOf(1.0, 5.0, 20.0))
        assertRenders(150, chart.height.toInt(), "$base/narrow.png", chart)
    }
}
