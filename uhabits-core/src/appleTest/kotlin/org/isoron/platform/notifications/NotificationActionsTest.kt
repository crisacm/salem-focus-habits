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

import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.utcTime
import platform.UserNotifications.UNNotificationDefaultActionIdentifier
import platform.UserNotifications.UNNotificationDismissActionIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotificationActionsTest {
    private val info: Map<Any?, *> = mapOf("habitId" to 7L, "timestamp" to 1_422_230_400_000L)

    @Test
    fun testYesAndNoBecomeCheckAndUncheck() {
        assertEquals(NotificationAction.Check(7, 1_422_230_400_000L), parseNotificationResponse(ACTION_YES, info))
        assertEquals(NotificationAction.Uncheck(7, 1_422_230_400_000L), parseNotificationResponse(ACTION_NO, info))
    }

    @Test
    fun testCheckAndUncheckExposeTheDayOfTheTimestamp() {
        val day = utcTime(2015, 1, 26)
        assertEquals(LocalDate(2015, 1, 26), NotificationAction.Check(7, day).date)
        assertEquals(LocalDate(2015, 1, 26), NotificationAction.Uncheck(7, day).date)
        assertEquals(LocalDate(1999, 12, 31), NotificationAction.Check(7, utcTime(1999, 12, 31)).date)
    }

    @Test
    fun testTappingTheNotificationOpensTheHabit() {
        assertEquals(
            NotificationAction.Open(7),
            parseNotificationResponse(UNNotificationDefaultActionIdentifier, info)
        )
    }

    @Test
    fun testOtherActionsAndBadDataAreIgnored() {
        assertNull(parseNotificationResponse(UNNotificationDismissActionIdentifier, info))
        assertNull(parseNotificationResponse("SOMETHING_ELSE", info))
        assertNull(parseNotificationResponse(ACTION_YES, emptyMap<Any?, Any?>()))
        assertNull(parseNotificationResponse(ACTION_YES, mapOf("habitId" to "seven")))
        assertNull(parseNotificationResponse(ACTION_YES, mapOf("habitId" to 7L)))
        assertNull(parseNotificationResponse(UNNotificationDefaultActionIdentifier, emptyMap<Any?, Any?>()))
    }

    @Test
    fun testRoundTripsTheUserInfoOfARealRequest() {
        val request = buildNotificationRequest(
            ReminderRequest("reminder-7", "Read", "Body", utcTime(2030, 1, 2, 13), 7, utcTime(2030, 1, 2), true)
        )
        assertEquals(
            NotificationAction.Check(7, utcTime(2030, 1, 2)),
            parseNotificationResponse(ACTION_YES, request.content.userInfo)
        )
    }

    @Test
    fun testScheduledRequestsCarryTheYesNoCategoryOnlyWhenAsked() {
        val with = buildNotificationRequest(ReminderRequest("a", "t", "b", utcTime(2030, 1, 2, 13), 1, 0, true))
        val without = buildNotificationRequest(ReminderRequest("a", "t", "b", utcTime(2030, 1, 2, 13), 1, 0, false))
        assertEquals(REMINDER_CATEGORY, with.content.categoryIdentifier)
        assertEquals("", without.content.categoryIdentifier)
    }

    @Test
    fun testImmediateRequestHasNoTriggerAndTheSameIdentifier() {
        val request = buildImmediateRequest(ShownNotification("reminder-7", "Read", "Body", 7, 55, true))
        assertEquals("reminder-7", request.identifier)
        assertNull(request.trigger)
        assertEquals(REMINDER_CATEGORY, request.content.categoryIdentifier)
        assertEquals(
            NotificationAction.Uncheck(7, 55),
            parseNotificationResponse(ACTION_NO, request.content.userInfo)
        )
    }

    @Test
    fun testCategoryHasYesAndNoActionsWithTheGivenTitles() {
        val category = buildReminderCategory("Yes!", "Nope")
        assertEquals(REMINDER_CATEGORY, category.identifier)
        val actions = category.actions.map { it as platform.UserNotifications.UNNotificationAction }
        assertEquals(listOf(ACTION_YES, ACTION_NO), actions.map { it.identifier })
        assertEquals(listOf("Yes!", "Nope"), actions.map { it.title })
        assertNotNull(actions.firstOrNull())
        assertTrue(actions.all { it.options == 0uL })
    }
}
