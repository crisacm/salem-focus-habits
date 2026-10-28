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

package io.github.crisacm.salem.ui.detail.charts

import io.github.crisacm.salem.platform.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartWindowTest {
    private val names = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private fun monthName(date: LocalDate) = names[date.month - 1]

    @Test
    fun theDataCannotMovePastItsOldestColumn() {
        assertEquals(70f, maxChartOffset(count = 10, columnPx = 10f, viewportPx = 30f))
        assertEquals(0f, maxChartOffset(count = 2, columnPx = 10f, viewportPx = 30f))
    }

    @Test
    fun theNewestColumnsAreInViewAtTheStart() {
        assertEquals(0..2, visibleColumns(count = 10, columnPx = 10f, viewportPx = 30f, offsetPx = 0f))
    }

    @Test
    fun aPartlyHiddenColumnCountsAsVisible() {
        assertEquals(0..3, visibleColumns(10, 10f, 30f, offsetPx = 5f))
        assertEquals(3..5, visibleColumns(10, 10f, 30f, offsetPx = 30f))
    }

    @Test
    fun theVisibleColumnsStayInsideTheData() {
        assertEquals(9..9, visibleColumns(10, 10f, 30f, offsetPx = 95f))
        assertTrue(visibleColumns(0, 10f, 30f, 0f).isEmpty())
    }

    @Test
    fun theNewestColumnTouchesTheRightEnd() {
        assertEquals(20f, columnLeft(index = 0, columnPx = 10f, viewportPx = 30f, offsetPx = 0f))
        assertEquals(10f, columnLeft(1, 10f, 30f, 0f))
        // Moving the data to the right pushes the newest column out of the viewport
        assertEquals(30f, columnLeft(0, 10f, 30f, offsetPx = 10f))
    }

    @Test
    fun theRangeNamesTheMonthsAndTheYear() {
        assertEquals("Apr 2021", rangeLabel(LocalDate(2021, 4, 1), LocalDate(2021, 4, 30), ::monthName))
        assertEquals("Jan \u2013 Apr 2021", rangeLabel(LocalDate(2021, 1, 10), LocalDate(2021, 4, 20), ::monthName))
        assertEquals("Mar 2020 \u2013 Apr 2021", rangeLabel(LocalDate(2020, 3, 5), LocalDate(2021, 4, 20), ::monthName))
    }

    @Test
    fun theScoreNamesTheOldestColumnOfEachMonth() {
        // Newest first: two weeks of February, three of January
        val dates = listOf(
            LocalDate(2021, 2, 15), LocalDate(2021, 2, 8), LocalDate(2021, 1, 25),
            LocalDate(2021, 1, 18), LocalDate(2021, 1, 11)
        )
        assertEquals(listOf(1, 4), axisMarks(dates, bucketSize = 7))
    }

    @Test
    fun aQuarterOrYearBucketNamesEveryColumn() {
        val dates = listOf(LocalDate(2021, 1, 1), LocalDate(2020, 1, 1), LocalDate(2019, 1, 1))
        assertEquals(listOf(0, 1, 2), axisMarks(dates, bucketSize = 365))
    }

    @Test
    fun everyBucketHasAColumnCount() {
        for (bucket in listOf(1, 7, 31, 92, 365)) {
            assertTrue(scoreColumnsInView(bucket) > 0)
            assertTrue(barColumnsInView(bucket) > 0)
        }
    }
}
