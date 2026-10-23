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

package io.github.crisacm.salem.platform.notifications

import io.github.crisacm.salem.platform.Synchronized
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.ui.NotificationTray

private const val DAYS_FROM_1970_TO_2000 = 10_957L
private const val MILLIS_PER_DAY = 86_400_000L

/**
 * [NotificationTray.SystemTray] over local notifications. A shown notification has its own
 * identifier, so it never replaces the pending reminder of the habit; removing it also removes the
 * delivered notifications of the scheduled reminders, so one habit never leaves two behind. Android's
 * "sticky" notifications have no iOS equivalent and are ignored.
 *
 * @param defaultBody text of the notification for habits without a question
 */
class AppleSystemTray(
    private val center: DeliveredNotificationCenter,
    private val defaultBody: String,
    private val logger: (String) -> Unit = { platform.Foundation.NSLog("%@", it) }
) : NotificationTray.SystemTray {
    private val shown = HashMap<Int, Long>()

    @Synchronized
    override fun showNotification(habit: Habit, notificationId: Int, date: LocalDate, reminderTime: Long) {
        val habitId = habit.id ?: return
        shown[notificationId] = habitId
        center.post(
            ShownNotification(
                shownIdentifierOf(habitId),
                habit.name,
                habit.question.ifBlank { defaultBody },
                habitId,
                (date.daysSince2000 + DAYS_FROM_1970_TO_2000) * MILLIS_PER_DAY,
                habit.type == HabitType.YES_NO
            )
        )
    }

    @Synchronized
    override fun removeNotification(notificationId: Int) {
        val habitId = shown.remove(notificationId) ?: notificationId.toLong()
        center.removeDeliveredOfHabit(habitId)
    }

    override fun log(msg: String) = logger("AppleSystemTray: $msg")

    companion object {
        fun shownIdentifierOf(habitId: Long) = "shown-$habitId"
    }
}
