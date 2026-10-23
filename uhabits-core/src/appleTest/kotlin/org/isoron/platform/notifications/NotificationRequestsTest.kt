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
import org.isoron.platform.time.utcTime
import platform.Foundation.NSNumber
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNCalendarNotificationTrigger
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NotificationRequestsTest {
    @AfterTest
    fun tearDown() {
        DateUtils.setFixedTimeZone(null)
    }

    @Test
    fun testBuildsARequestThatFiresAtTheGivenLocalTime() {
        // 2030-01-02 08:30 in the zone of the device
        val fireTime = DateUtils.applyTimezone(utcTime(2030, 1, 2, 8, 30))
        val request = buildNotificationRequest(
            ReminderRequest("reminder-7", "Read", "Did you read?", fireTime, 7, utcTime(2030, 1, 2))
        )
        assertEquals("reminder-7", request.identifier)
        assertEquals("Read", request.content.title)
        assertEquals("Did you read?", request.content.body)
        val trigger = request.trigger as UNCalendarNotificationTrigger
        assertFalse(trigger.repeats)
        val next = assertNotNull(trigger.nextTriggerDate())
        assertEquals(fireTime, (next.timeIntervalSince1970 * 1000).toLong())
    }

    @Test
    fun testKeepsTheHabitAndTheDayInTheUserInfo() {
        val request = buildNotificationRequest(
            ReminderRequest("reminder-7", "Read", "Body", utcTime(2030, 1, 2, 13), 7, utcTime(2030, 1, 2))
        )
        val info = request.content.userInfo
        assertEquals(7L, (info["habitId"] as NSNumber).longLongValue)
        assertEquals(utcTime(2030, 1, 2), (info["timestamp"] as NSNumber).longLongValue)
        assertTrue(request.content.sound != null)
    }
}
