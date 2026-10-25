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

package io.github.crisacm.salem.ui.canvas

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.platform.gui.Canvas
import io.github.crisacm.salem.platform.gui.DataView
import io.github.crisacm.salem.ui.compose.ChartScrollState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CoreChartTest {
    private val fonts = SalemFonts(FontFamily.Default, FontFamily.Default, FontFamily.Default)

    private class FakeChart : DataView {
        val clicks = mutableListOf<Pair<Double, Double>>()
        val longClicks = mutableListOf<Pair<Double, Double>>()
        var draws = 0
        override var dataOffset = 0
        override val dataColumnWidth = 10.0

        override fun draw(canvas: Canvas) {
            draws++
        }

        override fun onClick(x: Double, y: Double) {
            clicks += x to y
        }

        override fun onLongClick(x: Double, y: Double) {
            longClicks += x to y
        }
    }

    @Test
    fun aTapReachesTheChartWithItsPosition() = runComposeUiTest {
        val chart = FakeChart()
        setContent { CoreChart(chart, ChartScrollState(), fonts, Modifier.size(100.dp)) }
        onRoot().performTouchInput { click(Offset(20f, 30f)) }
        waitForIdle()
        assertEquals(1, chart.clicks.size)
        assertEquals(20.0, chart.clicks.single().first, 1.0)
        assertEquals(30.0, chart.clicks.single().second, 1.0)
    }

    @Test
    fun aLongPressReachesTheChart() = runComposeUiTest {
        val chart = FakeChart()
        setContent { CoreChart(chart, ChartScrollState(), fonts, Modifier.size(100.dp)) }
        onRoot().performTouchInput { longClick(Offset(40f, 50f)) }
        waitForIdle()
        assertEquals(1, chart.longClicks.size)
        assertTrue(chart.clicks.isEmpty())
    }

    @Test
    fun draggingScrollsTheDataAndTheChartSeesTheOffset() = runComposeUiTest {
        val chart = FakeChart()
        val scroll = ChartScrollState()
        setContent { CoreChart(chart, scroll, fonts, Modifier.size(200.dp)) }
        onRoot().performTouchInput { swipeRight() }
        waitForIdle()
        assertTrue(scroll.dataOffset > 0, "the data moved: ${scroll.dataOffset}")
        assertEquals(scroll.dataOffset, chart.dataOffset)
    }

    @Test
    fun theChartIsDrawn() = runComposeUiTest {
        val chart = FakeChart()
        setContent { CoreChart(chart, ChartScrollState(), fonts, Modifier.size(100.dp)) }
        waitForIdle()
        assertTrue(chart.draws > 0)
    }
}
