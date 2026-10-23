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

package org.isoron.platform.notifications

import org.isoron.platform.time.DateUtils
import org.isoron.platform.time.DayOfWeek
import org.isoron.platform.time.fixedOffsetZone
import org.isoron.platform.time.utcTime
import org.isoron.platform.time.zoneOffsetsOf
import org.isoron.uhabits.core.models.WeekdayList
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ReminderPlanningTest {
    @AfterTest
    fun tearDown() {
        DateUtils.setFixedTimeZone(null)
    }

    // Same indexing as ShowNotificationTask: index 0 is Saturday and index 1 is Sunday.
    private fun days(vararg enabled: DayOfWeek): WeekdayList {
        val flags = BooleanArray(7)
        for (day in enabled) flags[(day.daysSinceSunday + 1) % 7] = true
        return WeekdayList(flags)
    }

    // 2015-01-26 is a Monday
    private val monday8 = utcTime(2015, 1, 26, 8, 0)

    private val day = DateUtils.DAY_LENGTH

    @Test
    fun testEveryDayGivesOneTimePerDayOfTheHorizon() {
        DateUtils.setFixedTimeZone(fixedOffsetZone(0))
        assertEquals(
            (0L..6L).map { monday8 + it * day },
            upcomingReminderTimes(monday8, WeekdayList.EVERY_DAY, 7)
        )
        assertEquals(listOf(monday8, monday8 + day), upcomingReminderTimes(monday8, WeekdayList.EVERY_DAY, 2))
    }

    @Test
    fun testTheFirstTimeIsKeptWhenItsDayIsEnabled() {
        DateUtils.setFixedTimeZone(fixedOffsetZone(0))
        // The next Monday is eight days away, outside the horizon
        assertEquals(listOf(monday8), upcomingReminderTimes(monday8, days(DayOfWeek.MONDAY), 7))
    }

    @Test
    fun testOnlyEnabledDaysAreReturned() {
        DateUtils.setFixedTimeZone(fixedOffsetZone(0))
        assertEquals(
            listOf(utcTime(2015, 1, 28, 8, 0), utcTime(2015, 1, 30, 8, 0)),
            upcomingReminderTimes(monday8, days(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), 7)
        )
        // The week wraps around: from Monday to the following Sunday is six days.
        assertEquals(
            listOf(utcTime(2015, 2, 1, 8, 0)),
            upcomingReminderTimes(monday8, days(DayOfWeek.SUNDAY), 7)
        )
    }

    @Test
    fun testNoEnabledDayOrNoHorizonGivesNothing() {
        DateUtils.setFixedTimeZone(fixedOffsetZone(0))
        assertEquals(emptyList(), upcomingReminderTimes(monday8, days(), 7))
        assertEquals(emptyList(), upcomingReminderTimes(monday8, WeekdayList.EVERY_DAY, 0))
    }

    @Test
    fun testTheLocalDayDecidesNotTheUtcDay() {
        // 02:00 UTC on Monday is still Sunday evening at UTC-5.
        DateUtils.setFixedTimeZone(fixedOffsetZone(-5 * DateUtils.HOUR_LENGTH))
        val mondayUtc = utcTime(2015, 1, 26, 2, 0)
        assertEquals(listOf(mondayUtc), upcomingReminderTimes(mondayUtc, days(DayOfWeek.SUNDAY), 7))
        assertEquals(listOf(mondayUtc + day), upcomingReminderTimes(mondayUtc, days(DayOfWeek.MONDAY), 7))
    }

    @Test
    fun testKeepsTheLocalHourAcrossDaylightSaving() {
        DateUtils.setFixedTimeZone(assertNotNull(zoneOffsetsOf("Australia/Sydney")))
        // Saturday 2017-09-30 08:30 in Sydney (UTC+10); daylight saving starts that night.
        val saturday = utcTime(2017, 9, 29, 22, 30)
        // Sunday 08:30 is UTC+11, so only 23 hours later.
        assertEquals(
            listOf(utcTime(2017, 9, 30, 21, 30)),
            upcomingReminderTimes(saturday, days(DayOfWeek.SUNDAY), 7)
        )
        assertEquals(
            listOf(saturday, utcTime(2017, 9, 30, 21, 30), utcTime(2017, 10, 1, 21, 30)),
            upcomingReminderTimes(saturday, WeekdayList.EVERY_DAY, 3)
        )
    }

    @Test
    fun testNothingIsDroppedUnderTheLimit() {
        val pending = listOf(PendingReminder("a", 3), PendingReminder("b", 1))
        assertEquals(emptyList(), reminderIdsToDrop(pending, 2))
        assertEquals(emptyList(), reminderIdsToDrop(emptyList(), 64))
    }

    @Test
    fun testTheLatestRemindersAreDroppedOverTheLimit() {
        val pending = listOf(
            PendingReminder("late", 50),
            PendingReminder("early", 10),
            PendingReminder("middle", 30),
            PendingReminder("last", 90)
        )
        assertEquals(listOf("late", "last"), reminderIdsToDrop(pending, 2))
    }

    @Test
    fun testTiesAreBrokenByIdentifier() {
        val pending = listOf(PendingReminder("b", 5), PendingReminder("a", 5), PendingReminder("c", 5))
        assertEquals(listOf("c"), reminderIdsToDrop(pending, 2))
    }
}
