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
package io.github.crisacm.salem.inject

import android.content.Context
import io.github.crisacm.salem.core.AppScope
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.io.GenericImporter
import io.github.crisacm.salem.core.io.Logging
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.ModelFactory
import io.github.crisacm.salem.core.models.sqlite.SQLModelFactory
import io.github.crisacm.salem.core.models.sqlite.SQLiteHabitList
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.preferences.WidgetPreferences
import io.github.crisacm.salem.core.reminders.ReminderScheduler
import io.github.crisacm.salem.core.tasks.CoroutineTaskRunner
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.ui.NotificationTray
import io.github.crisacm.salem.core.ui.screens.habits.list.HabitCardListCache
import io.github.crisacm.salem.core.utils.MidnightTimer
import io.github.crisacm.salem.database.AndroidDatabase
import io.github.crisacm.salem.database.AndroidDatabaseOpener
import io.github.crisacm.salem.intents.IntentFactory
import io.github.crisacm.salem.intents.IntentParser
import io.github.crisacm.salem.intents.IntentScheduler
import io.github.crisacm.salem.intents.PendingIntentFactory
import io.github.crisacm.salem.io.AndroidLogging
import io.github.crisacm.salem.notifications.AndroidNotificationTray
import io.github.crisacm.salem.platform.io.AndroidFileOpener
import io.github.crisacm.salem.platform.io.DatabaseOpener
import io.github.crisacm.salem.platform.io.FileOpener
import io.github.crisacm.salem.preferences.SharedPreferencesStorage
import io.github.crisacm.salem.receivers.ReminderController
import io.github.crisacm.salem.utils.DatabaseUtils
import io.github.crisacm.salem.widgets.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import java.io.File

@AppScope
@Component
abstract class HabitsApplicationComponent(
    @get:Provides @get:AppContext
    val appContext: Context,
    @get:Provides val dbFile: File
) {
    abstract val commandRunner: CommandRunner

    @get:AppContext
    abstract val context: Context
    abstract val genericImporter: GenericImporter
    abstract val habitCardListCache: HabitCardListCache
    abstract val habitList: HabitList
    abstract val intentFactory: IntentFactory
    abstract val intentParser: IntentParser
    abstract val logging: Logging
    abstract val midnightTimer: MidnightTimer
    abstract val modelFactory: ModelFactory
    abstract val notificationTray: NotificationTray
    abstract val pendingIntentFactory: PendingIntentFactory
    abstract val preferences: Preferences
    abstract val reminderScheduler: ReminderScheduler
    abstract val reminderController: ReminderController
    abstract val taskRunner: TaskRunner
    abstract val widgetPreferences: WidgetPreferences
    abstract val widgetUpdater: WidgetUpdater

    val db: AndroidDatabase
        get() = providedDb

    private val providedDb: AndroidDatabase by lazy {
        AndroidDatabase(DatabaseUtils.openDatabase())
    }

    @AppScope
    @Provides
    open fun preferences(storage: SharedPreferencesStorage): Preferences =
        Preferences(storage)

    @AppScope
    @Provides
    open fun reminderScheduler(
        sys: IntentScheduler,
        commandRunner: CommandRunner,
        habitList: HabitList,
        widgetPreferences: WidgetPreferences
    ): ReminderScheduler =
        ReminderScheduler(commandRunner, habitList, sys, widgetPreferences)

    @AppScope
    @Provides
    open fun notificationTray(
        taskRunner: TaskRunner,
        commandRunner: CommandRunner,
        preferences: Preferences,
        screen: AndroidNotificationTray
    ): NotificationTray =
        NotificationTray(taskRunner, commandRunner, preferences, screen)

    @AppScope
    @Provides
    open fun widgetPreferences(storage: SharedPreferencesStorage): WidgetPreferences =
        WidgetPreferences(storage)

    @AppScope
    @Provides
    open fun modelFactory(): ModelFactory = SQLModelFactory(providedDb)

    @AppScope
    @Provides
    open fun habitList(list: SQLiteHabitList): HabitList = list

    @AppScope
    @Provides
    open fun databaseOpener(opener: AndroidDatabaseOpener): DatabaseOpener = opener

    @AppScope
    @Provides
    open fun logging(): Logging = AndroidLogging()

    @AppScope
    @Provides
    open fun taskRunner(): TaskRunner = CoroutineTaskRunner(
        mainDispatcher = Dispatchers.Main,
        ioDispatcher = Dispatchers.IO
    )

    @AppScope
    @Provides
    open fun fileOpener(): FileOpener =
        AndroidFileOpener(appContext.assets, appContext.filesDir)
}
