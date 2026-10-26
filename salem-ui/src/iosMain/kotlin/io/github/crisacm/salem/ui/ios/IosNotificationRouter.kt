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

package io.github.crisacm.salem.ui.ios

import io.github.crisacm.salem.platform.notifications.AppleNotificationDelegate
import io.github.crisacm.salem.platform.notifications.NotificationAction
import io.github.crisacm.salem.ui.reminders.ReminderActions
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Receives what the user does with a reminder notification and passes it to the app. The notification center keeps
 * its delegate weakly and iOS expects it to be set while the app launches (a tap on a notification can be what
 * launches it), so [install] runs as soon as the root view is created, before the habits are loaded; the actions that
 * arrive until [attach] gives the app's [ReminderActions] wait in a queue.
 *
 * The center calls the delegate from any thread. Everything here runs on the main thread, as the reminder classes
 * (which are not synchronized on iOS) expect.
 */
object IosNotificationRouter {
    private val delegate = AppleNotificationDelegate { action -> onMain { handle(action) } }
    private var actions: ReminderActions? = null
    private val waiting = mutableListOf<NotificationAction>()

    fun install() {
        UNUserNotificationCenter.currentNotificationCenter().delegate = delegate
    }

    /** From now on the actions go to [reminderActions]; the ones that arrived earlier go first. */
    fun attach(reminderActions: ReminderActions) = onMain {
        actions = reminderActions
        val queued = waiting.toList()
        waiting.clear()
        queued.forEach(::handle)
    }

    private fun handle(action: NotificationAction) {
        val target = actions ?: run {
            waiting += action
            return
        }
        when (action) {
            is NotificationAction.Open -> target.open(action.habitId)
            is NotificationAction.Check -> target.check(action.habitId, action.date)
            is NotificationAction.Uncheck -> target.uncheck(action.habitId, action.date)
        }
    }

    private fun onMain(block: () -> Unit) = dispatch_async(dispatch_get_main_queue()) { block() }
}
