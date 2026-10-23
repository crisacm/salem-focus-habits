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

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DateUtilsCommonTest {
    private val hour = DateUtils.HOUR_LENGTH
    private val minute = DateUtils.MINUTE_LENGTH

    // 2017-01-01T00:00:00Z
    private val jan1 = 1_483_228_800_000L

    @AfterTest
    fun tearDown() {
        DateUtils.setFixedLocalTime(null)
        DateUtils.setFixedTimeZone(null)
    }

    private fun gmt() = assertNotNull(zoneOffsetsOf("GMT"))

    private fun sydney() = assertNotNull(zoneOffsetsOf("Australia/Sydney"))

    @Test
    fun testZoneOffsetsOfKnownAndUnknownZones() {
        assertEquals(0, gmt().offsetAt(jan1))
        assertEquals(11 * hour, sydney().offsetAt(jan1))
        assertEquals(10 * hour, sydney().offsetAt(1_501_401_600_000L))
        assertEquals(null, zoneOffsetsOf("Not/AZone"))
    }

    @Test
    fun testSystemZoneAndClockAreAvailable() {
        val now = currentTimeMillis()
        assertEquals(true, now > jan1)
        systemZoneOffsets().offsetAt(now)
    }

    @Test
    fun testGetStartOfDay() {
        assertEquals(jan1, DateUtils.getStartOfDay(jan1 + 23 * hour + 59 * minute))
        assertEquals(jan1 + DateUtils.DAY_LENGTH, DateUtils.getStartOfDay(jan1 + DateUtils.DAY_LENGTH))
    }

    @Test
    fun testGetStartOfDayWithOffset() {
        assertEquals(jan1, DateUtils.getStartOfDayWithOffset(jan1 + hour, 0, 0))
        assertEquals(jan1 - DateUtils.DAY_LENGTH, DateUtils.getStartOfDayWithOffset(jan1 + 3 * hour + 29 * minute, 3, 30))
        assertEquals(jan1, DateUtils.getStartOfDayWithOffset(jan1 + 3 * hour + 30 * minute, 3, 30))
    }

    @Test
    fun testApplyAndRemoveTimezoneAcrossDaylightSaving() {
        DateUtils.setFixedTimeZone(sydney())
        // Winter, UTC+10: 2017-07-30 18:00 local is 08:00 UTC.
        assertEquals(1_501_401_600_000L, DateUtils.applyTimezone(1_501_437_600_000L))
        assertEquals(1_501_437_600_000L, DateUtils.removeTimezone(1_501_401_600_000L))
        // Daylight saving starts 2017-10-01 02:00 local: before it UTC+10, after it UTC+11.
        assertEquals(1_506_787_140_000L, DateUtils.applyTimezone(1_506_823_140_000L))
        assertEquals(1_506_787_200_000L, DateUtils.applyTimezone(1_506_826_800_000L))
        assertEquals(1_506_826_800_000L, DateUtils.removeTimezone(1_506_787_200_000L))
    }

    @Test
    fun testGetLocalTimeUsesTheFixedTimeAndTheZone() {
        DateUtils.setFixedTimeZone(sydney())
        assertEquals(jan1 + 11 * hour, DateUtils.getLocalTime(utcTimeInMillis = jan1))
        DateUtils.setFixedLocalTime(123L)
        assertEquals(123L, DateUtils.getLocalTime())
    }

    @Test
    fun testGetUpcomingTimeInMillisPicksTodayOrTomorrow() {
        DateUtils.setFixedTimeZone(gmt())
        DateUtils.setFixedLocalTime(jan1 + 9 * hour)
        assertEquals(jan1 + 10 * hour + minute, DateUtils.getUpcomingTimeInMillis(10, 1))
        DateUtils.setFixedLocalTime(jan1 + 11 * hour)
        assertEquals(jan1 + DateUtils.DAY_LENGTH + 10 * hour + minute, DateUtils.getUpcomingTimeInMillis(10, 1))
    }

    @Test
    fun testMillisecondsUntilTomorrowOnADayOf23Hours() {
        DateUtils.setFixedTimeZone(sydney())
        DateUtils.setFixedLocalTime(utcTime(2017, 10, 1) + 1)
        assertEquals(23 * hour - 1, DateUtils.millisecondsUntilTomorrowWithOffset(0, 0))
    }

    @Test
    fun testMillisecondsUntilTomorrow() {
        DateUtils.setFixedTimeZone(gmt())
        DateUtils.setFixedLocalTime(jan1 + 23 * hour + 59 * minute)
        assertEquals(minute, DateUtils.millisecondsUntilTomorrowWithOffset(0, 0))
        DateUtils.setFixedLocalTime(jan1 + 20 * hour)
        assertEquals(4 * hour, DateUtils.millisecondsUntilTomorrowWithOffset(0, 0))
        DateUtils.setFixedLocalTime(jan1 + 23 * hour + 59 * minute)
        assertEquals(3 * hour + 31 * minute, DateUtils.millisecondsUntilTomorrowWithOffset(3, 30))
        DateUtils.setFixedLocalTime(jan1 + DateUtils.DAY_LENGTH + hour)
        assertEquals(2 * hour + 30 * minute, DateUtils.millisecondsUntilTomorrowWithOffset(3, 30))
    }
}
