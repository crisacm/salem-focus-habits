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
import platform.Foundation.NSNumber
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionNone
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionNone
import platform.UserNotifications.UNNotificationDefaultActionIdentifier
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

const val REMINDER_CATEGORY = "HABIT_REMINDER"
const val ACTION_YES = "YES"
const val ACTION_NO = "NO"

/**
 * What the user did with a reminder: pressed Yes or No, or tapped the notification.
 * [timestamp] is the start of the day the reminder was about.
 */
sealed class NotificationAction {
    data class Check(val habitId: Long, val timestamp: Long) : NotificationAction() {
        val date: LocalDate get() = dayOf(timestamp)
    }

    data class Uncheck(val habitId: Long, val timestamp: Long) : NotificationAction() {
        val date: LocalDate get() = dayOf(timestamp)
    }

    data class Open(val habitId: Long) : NotificationAction()
}

private fun dayOf(timestamp: Long): LocalDate =
    LocalDate((timestamp.floorDiv(DateUtils.DAY_LENGTH) - 10_957L).toInt())

/**
 * Translates the response of the notification center. Returns null for actions that are not ours
 * (for example dismissing) and for notifications without the habit data.
 */
fun parseNotificationResponse(actionIdentifier: String, userInfo: Map<Any?, *>): NotificationAction? {
    val habitId = asLong(userInfo["habitId"]) ?: return null
    return when (actionIdentifier) {
        UNNotificationDefaultActionIdentifier -> NotificationAction.Open(habitId)
        ACTION_YES -> asLong(userInfo["timestamp"])?.let { NotificationAction.Check(habitId, it) }
        ACTION_NO -> asLong(userInfo["timestamp"])?.let { NotificationAction.Uncheck(habitId, it) }
        else -> null
    }
}

private fun asLong(value: Any?): Long? = when (value) {
    is Long -> value
    is NSNumber -> value.longLongValue
    else -> null
}

/**
 * A notification to show right now.
 */
data class ShownNotification(
    val identifier: String,
    val title: String,
    val body: String,
    val habitId: Long,
    val timestamp: Long,
    val withActions: Boolean
)

/**
 * The part of the notification center used to show and remove notifications that were delivered.
 * An interface for the same reason as [PendingNotificationCenter].
 */
interface DeliveredNotificationCenter {
    /**
     * Shows the notification, replacing a delivered or pending one with the same identifier.
     */
    fun post(notification: ShownNotification)

    /**
     * Removes the delivered notifications of the habit: the shown one and those of its scheduled reminders.
     */
    fun removeDeliveredOfHabit(habitId: Long)
}

/**
 * The identifiers, among [identifiers], of the notifications of the habit: `shown-<id>` and
 * `reminder-<id>-<day>` (and not those of habit 70 when the habit is 7).
 */
internal fun identifiersOfHabit(identifiers: List<String>, habitId: Long): List<String> =
    identifiers.filter { it == AppleSystemTray.shownIdentifierOf(habitId) || it.startsWith("reminder-$habitId-") }

internal fun buildImmediateRequest(notification: ShownNotification): UNNotificationRequest {
    val content = UNMutableNotificationContent()
    content.setTitle(notification.title)
    content.setBody(notification.body)
    content.setSound(UNNotificationSound.defaultSound)
    content.setUserInfo(mapOf("habitId" to notification.habitId, "timestamp" to notification.timestamp))
    if (notification.withActions) content.setCategoryIdentifier(REMINDER_CATEGORY)
    return UNNotificationRequest.requestWithIdentifier(notification.identifier, content, null)
}

/**
 * The category with the Yes and No buttons. The app registers it when it starts, with
 * `UNUserNotificationCenter.setNotificationCategories`, using translated titles.
 */
fun buildReminderCategory(yesTitle: String, noTitle: String): UNNotificationCategory {
    val yes = UNNotificationAction.actionWithIdentifier(ACTION_YES, yesTitle, UNNotificationActionOptionNone)
    val no = UNNotificationAction.actionWithIdentifier(ACTION_NO, noTitle, UNNotificationActionOptionNone)
    return UNNotificationCategory.categoryWithIdentifier(
        REMINDER_CATEGORY,
        listOf(yes, no),
        emptyList<String>(),
        UNNotificationCategoryOptionNone
    )
}

/**
 * [DeliveredNotificationCenter] over the real notification center. Only usable inside an app bundle.
 */
class UNDeliveredNotificationCenter(
    private val center: UNUserNotificationCenter = UNUserNotificationCenter.currentNotificationCenter()
) : DeliveredNotificationCenter {
    override fun post(notification: ShownNotification) {
        center.addNotificationRequest(buildImmediateRequest(notification), withCompletionHandler = null)
    }

    override fun removeDeliveredOfHabit(habitId: Long) {
        center.getDeliveredNotificationsWithCompletionHandler { delivered ->
            val identifiers = delivered.orEmpty().map { (it as UNNotification).request.identifier }
            val ours = identifiersOfHabit(identifiers, habitId)
            if (ours.isNotEmpty()) center.removeDeliveredNotificationsWithIdentifiers(ours)
        }
    }
}

/**
 * Delegate of the notification center: reports the buttons the user pressed through [onAction] and
 * lets notifications that arrive while the app is open show as banners with sound. The app sets it
 * as `UNUserNotificationCenter.delegate` (the center keeps only a weak reference, so keep a strong one).
 */
class AppleNotificationDelegate(
    private val onAction: (NotificationAction) -> Unit
) : NSObject(), UNUserNotificationCenterDelegateProtocol {
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        val content = didReceiveNotificationResponse.notification.request.content
        parseNotificationResponse(didReceiveNotificationResponse.actionIdentifier, content.userInfo)?.let(onAction)
        withCompletionHandler()
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (ULong) -> Unit
    ) {
        withCompletionHandler(UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionSound)
    }
}
