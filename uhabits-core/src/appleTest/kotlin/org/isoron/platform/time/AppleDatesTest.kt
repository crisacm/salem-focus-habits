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

package org.isoron.platform.time

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppleDatesTest {
    private val hour = 3_600_000L
    private val minute = 60_000L

    // 2000-01-01T00:00:00Z
    private val y2000 = 946_684_800_000L

    @Test
    fun testFirstWeekdayNumberIsBetweenOneAndSeven() {
        val number = getFirstWeekdayNumberAccordingToLocale()
        assertTrue(number in 1..7, "Unexpected first weekday: $number")
        assertEquals(DayOfWeek.entries[number - 1], getFirstWeekday())
    }

    @Test
    fun testDaysSince2000AtTheEpochs() {
        assertEquals(-10957, daysSince2000(0, 0, 0, 0))
        assertEquals(0, daysSince2000(y2000, 0, 0, 0))
        assertEquals(0, daysSince2000(y2000 + 24 * hour - 1, 0, 0, 0))
        assertEquals(1, daysSince2000(y2000 + 24 * hour, 0, 0, 0))
    }

    @Test
    fun testDaysSince2000AppliesTheZoneOffset() {
        // 23:00 UTC is already the next day at UTC+2, and still the same day at UTC-5.
        val t = y2000 + 23 * hour
        assertEquals(1, daysSince2000(t, 2 * hour, 0, 0))
        assertEquals(0, daysSince2000(t, -5 * hour, 0, 0))
        // 01:00 UTC is still the previous day at UTC-5.
        assertEquals(-1, daysSince2000(y2000 + hour, -5 * hour, 0, 0))
    }

    @Test
    fun testDaysSince2000RoundsDownBeforeTheEpoch() {
        // 1969-12-31T23:59:59.999Z must be day -1, not day 0 (truncation).
        assertEquals(-10958, daysSince2000(-1, 0, 0, 0))
    }

    @Test
    fun testDaysSince2000DelaysTheStartOfTheDay() {
        val threeAm = y2000 + 3 * hour
        assertEquals(-1, daysSince2000(threeAm - 1, 0, 3, 0))
        assertEquals(0, daysSince2000(threeAm, 0, 3, 0))
        val halfPastTwo = y2000 + 2 * hour + 30 * minute
        assertEquals(-1, daysSince2000(halfPastTwo - 1, 0, 2, 30))
        assertEquals(0, daysSince2000(halfPastTwo, 0, 2, 30))
    }

    @Test
    fun testComputeTodayIsConsistentWithDaysSince2000() {
        val before = computeToday()
        val after = computeToday()
        assertTrue(before.year in 2020..2100)
        assertTrue(after >= before)
        // A 24 hour delay always yields the previous day.
        assertEquals(computeToday().daysSince2000 - 1, computeToday(24, 0).daysSince2000)
    }
}
