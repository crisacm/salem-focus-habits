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

package io.github.crisacm.salem.kit

import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.io.Logging
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.preferences.UserDefaultsStorage
import io.github.crisacm.salem.core.preferences.WidgetPreferences
import io.github.crisacm.salem.core.reminders.ReminderScheduler
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.ui.NotificationTray
import io.github.crisacm.salem.core.ui.widgets.WidgetBehavior
import io.github.crisacm.salem.core.utils.MidnightTimer
import io.github.crisacm.salem.platform.notifications.AppForegroundRefresher
import io.github.crisacm.salem.platform.notifications.AppleNotificationDelegate
import io.github.crisacm.salem.platform.notifications.AppleReminderSync
import io.github.crisacm.salem.platform.notifications.AppleSystemScheduler
import io.github.crisacm.salem.platform.notifications.AppleSystemTray
import io.github.crisacm.salem.platform.notifications.HabitCompletionRearmer
import io.github.crisacm.salem.platform.notifications.NotificationAction
import io.github.crisacm.salem.platform.notifications.UNDeliveredNotificationCenter
import io.github.crisacm.salem.platform.notifications.UNPendingNotificationCenter
import io.github.crisacm.salem.platform.notifications.buildReminderCategory
import io.github.crisacm.salem.platform.time.LocalDate
import kotlinx.coroutines.Dispatchers
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/** Reminders for the SwiftUI app: scheduling stays in Kotlin, the permission prompt is Swift. */
class KitReminders(
    private val systemScheduler: AppleSystemScheduler,
    val scheduler: ReminderScheduler,
    val refresher: AppForegroundRefresher,
    val actions: KitReminderActions
) {
    fun rescheduleAll() {
        systemScheduler.clear()
        scheduler.scheduleAll()
    }

    companion object {
        fun start(
            habitList: HabitList,
            commandRunner: CommandRunner,
            taskRunner: TaskRunner,
            preferences: Preferences,
            logging: Logging,
            defaultBody: String,
            yes: String,
            no: String,
            onOpenHabit: (Long) -> Unit
        ): KitReminders {
            val systemScheduler = AppleSystemScheduler(UNPendingNotificationCenter(), defaultBody)
            val scheduler = ReminderScheduler(
                commandRunner,
                habitList,
                systemScheduler,
                WidgetPreferences(UserDefaultsStorage())
            )
            val tray = NotificationTray(
                taskRunner,
                commandRunner,
                preferences,
                AppleSystemTray(UNDeliveredNotificationCenter(), defaultBody)
            )
            AppleReminderSync(commandRunner, scheduler, systemScheduler).startListening()
            commandRunner.addListener(HabitCompletionRearmer(scheduler) { preferences.midnightDelayHours })
            tray.startListening()
            UNUserNotificationCenter.currentNotificationCenter().setNotificationCategories(
                setOf(buildReminderCategory(yes, no))
            )
            val midnight = MidnightTimer(logging, preferences, Dispatchers.Main)
            return KitReminders(
                systemScheduler,
                scheduler,
                AppForegroundRefresher(preferences, midnight, scheduler, systemScheduler),
                KitReminderActions(habitList, WidgetBehavior(habitList, commandRunner, tray, preferences), onOpenHabit)
            )
        }
    }
}

class KitReminderActions(
    private val habitList: HabitList,
    private val widgetBehavior: WidgetBehavior,
    private val onOpenHabit: (Long) -> Unit
) {
    fun open(habitId: Long) {
        if (active(habitId)) onOpenHabit(habitId)
    }

    fun check(habitId: Long, date: LocalDate) {
        habitList.getById(habitId)?.takeUnless { it.isArchived }?.let { widgetBehavior.onAddRepetition(it, date) }
    }

    fun uncheck(habitId: Long, date: LocalDate) {
        habitList.getById(habitId)?.takeUnless { it.isArchived }?.let { widgetBehavior.onRemoveRepetition(it, date) }
    }

    private fun active(habitId: Long) = habitList.getById(habitId)?.isArchived == false
}

/**
 * The notification center keeps its delegate weakly, so [install] runs as the session opens. Actions that arrive
 * before [attach] wait. The center calls back from any thread; the reminder classes expect the main thread.
 */
object KitNotificationRouter {
    private val delegate = AppleNotificationDelegate { action -> onMain { handle(action) } }
    private var actions: KitReminderActions? = null
    private val waiting = mutableListOf<NotificationAction>()

    fun install() {
        UNUserNotificationCenter.currentNotificationCenter().delegate = delegate
    }

    fun attach(reminderActions: KitReminderActions) = onMain {
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
