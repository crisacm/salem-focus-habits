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

import io.github.crisacm.salem.core.commands.CommandRunner
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
import io.github.crisacm.salem.platform.notifications.AppleReminderSync
import io.github.crisacm.salem.platform.notifications.AppleSystemScheduler
import io.github.crisacm.salem.platform.notifications.AppleSystemTray
import io.github.crisacm.salem.platform.notifications.HabitCompletionRearmer
import io.github.crisacm.salem.platform.notifications.UNDeliveredNotificationCenter
import io.github.crisacm.salem.platform.notifications.UNPendingNotificationCenter
import io.github.crisacm.salem.platform.notifications.buildReminderCategory
import io.github.crisacm.salem.ui.detail.DetailNavigation
import io.github.crisacm.salem.ui.reminders.ReminderActions
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.default_reminder_question
import io.github.crisacm.salem.ui.resources.no
import io.github.crisacm.salem.ui.resources.yes
import org.jetbrains.compose.resources.getString
import platform.UserNotifications.UNUserNotificationCenter

/**
 * The reminders and notifications of the iOS app, wired the way `HabitsApplication` does it on Android: the scheduler
 * with the system scheduler of iOS, the tray, the listeners of the commands, the refresh when the app comes back and
 * the actions of the notification buttons. [create] starts the listeners; [refresher] must be launched once the habits
 * are on screen. Everything here runs on the main thread (the classes of iOS are not synchronized), which is where the
 * task runner reports finished commands.
 */
class IosReminders private constructor(
    val reminderScheduler: ReminderScheduler,
    val notificationTray: NotificationTray,
    val refresher: AppForegroundRefresher,
    val actions: ReminderActions
) {
    companion object {
        suspend fun create(
            habitList: HabitList,
            commandRunner: CommandRunner,
            taskRunner: TaskRunner,
            preferences: Preferences,
            midnightTimer: MidnightTimer,
            navigation: DetailNavigation
        ): IosReminders {
            val defaultBody = getString(Res.string.default_reminder_question)
            val systemScheduler = AppleSystemScheduler(UNPendingNotificationCenter(), defaultBody)
            val reminderScheduler = ReminderScheduler(
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

            // A command that changes habits clears and schedules everything again (AppleReminderSync); a check moves
            // the reminder of the day (HabitCompletionRearmer); the tray removes the delivered notification of a habit
            // that was checked or deleted
            AppleReminderSync(commandRunner, reminderScheduler, systemScheduler).startListening()
            commandRunner.addListener(HabitCompletionRearmer(reminderScheduler) { preferences.midnightDelayHours })
            tray.startListening()

            UNUserNotificationCenter.currentNotificationCenter().setNotificationCategories(
                setOf(buildReminderCategory(getString(Res.string.yes), getString(Res.string.no)))
            )
            val actions = ReminderActions(
                habitList,
                WidgetBehavior(habitList, commandRunner, tray, preferences),
                navigation
            )
            IosNotificationRouter.attach(actions)
            return IosReminders(
                reminderScheduler,
                tray,
                AppForegroundRefresher(preferences, midnightTimer, reminderScheduler, systemScheduler),
                actions
            )
        }
    }
}
