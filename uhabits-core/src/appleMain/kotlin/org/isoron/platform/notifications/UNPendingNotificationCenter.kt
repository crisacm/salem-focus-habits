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
import org.isoron.platform.time.LocalDate
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

private const val DAYS_FROM_1970_TO_2000 = 10_957L

/**
 * Builds the one-shot calendar notification for [request], using the local date and time of the
 * device zone so that it fires at the same wall clock time the user chose.
 */
internal fun buildNotificationRequest(request: ReminderRequest): UNNotificationRequest {
    val localTime = DateUtils.removeTimezone(request.fireTimeMillis)
    val dayCount = localTime.floorDiv(DateUtils.DAY_LENGTH)
    val date = LocalDate((dayCount - DAYS_FROM_1970_TO_2000).toInt())
    val secondsOfDay = (localTime - dayCount * DateUtils.DAY_LENGTH) / DateUtils.SECOND_LENGTH
    val parts = NSDateComponents()
    parts.year = date.year.toLong()
    parts.month = date.month.toLong()
    parts.day = date.day.toLong()
    parts.hour = secondsOfDay / 3600
    parts.minute = secondsOfDay % 3600 / 60
    parts.second = secondsOfDay % 60
    val content = UNMutableNotificationContent()
    content.setTitle(request.title)
    content.setBody(request.body)
    content.setSound(UNNotificationSound.defaultSound)
    content.setUserInfo(mapOf("habitId" to request.habitId, "timestamp" to request.timestamp))
    val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(parts, false)
    return UNNotificationRequest.requestWithIdentifier(request.identifier, content, trigger)
}

/**
 * [PendingNotificationCenter] over the real notification center. Only usable inside an app bundle.
 */
class UNPendingNotificationCenter(
    private val center: UNUserNotificationCenter = UNUserNotificationCenter.currentNotificationCenter()
) : PendingNotificationCenter {
    override fun add(request: ReminderRequest) {
        center.addNotificationRequest(buildNotificationRequest(request), withCompletionHandler = null)
    }

    override fun remove(identifier: String) {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier))
    }

    override fun removeAll() {
        center.removeAllPendingNotificationRequests()
    }
}
