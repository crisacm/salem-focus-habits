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
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
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

    // Two pixels per dp in the whole window, so a mistake between px and dp shows in the position that the chart receives
    private fun runAtTwoPixelsPerDp(block: androidx.compose.ui.test.SkikoComposeUiTest.() -> Unit) =
        runSkikoComposeUiTest(density = Density(2f), block = block)

    private fun androidx.compose.ui.test.ComposeUiTest.show(chart: FakeChart, scroll: ChartScrollState = ChartScrollState()) =
        setContent { CoreChart(chart, scroll, fonts, Modifier.size(100.dp)) }

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
    fun aTapReachesTheChartWithItsPosition() = runAtTwoPixelsPerDp {
        val chart = FakeChart()
        show(chart)
        onRoot().performTouchInput { click(Offset(40f, 60f)) }
        waitForIdle()
        assertEquals(1, chart.clicks.size)
        assertEquals(20.0, chart.clicks.single().first, 1.0)
        assertEquals(30.0, chart.clicks.single().second, 1.0)
    }

    @Test
    fun aLongPressReachesTheChart() = runAtTwoPixelsPerDp {
        val chart = FakeChart()
        show(chart)
        onRoot().performTouchInput { longClick(Offset(80f, 100f)) }
        waitForIdle()
        assertEquals(1, chart.longClicks.size)
        assertEquals(40.0, chart.longClicks.single().first, 1.0)
        assertEquals(50.0, chart.longClicks.single().second, 1.0)
        assertTrue(chart.clicks.isEmpty())
    }

    @Test
    fun draggingScrollsTheDataAndTheChartSeesTheOffset() = runAtTwoPixelsPerDp {
        val chart = FakeChart()
        val scroll = ChartScrollState()
        show(chart, scroll)
        onRoot().performTouchInput { swipeRight() }
        waitForIdle()
        assertTrue(scroll.dataOffset > 0, "the data moved: ${scroll.dataOffset}")
        assertEquals(scroll.dataOffset, chart.dataOffset)
    }

    @Test
    fun theChartIsDrawn() = runAtTwoPixelsPerDp {
        val chart = FakeChart()
        show(chart)
        waitForIdle()
        assertTrue(chart.draws > 0)
    }
}
