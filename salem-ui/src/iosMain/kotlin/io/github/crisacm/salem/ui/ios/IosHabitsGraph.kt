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

import io.github.crisacm.salem.core.APP_GROUP_ID
import io.github.crisacm.salem.core.DATABASE_FILENAME
import io.github.crisacm.salem.core.commands.Command
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.database.initializeHabitsSchema
import io.github.crisacm.salem.core.io.ExternalChanges
import io.github.crisacm.salem.core.io.Logging
import io.github.crisacm.salem.core.io.StandardLogging
import io.github.crisacm.salem.core.io.moveDatabase
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.sqlite.SQLModelFactory
import io.github.crisacm.salem.core.models.sqlite.SQLiteHabitList
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.preferences.UserDefaultsStorage
import io.github.crisacm.salem.core.tasks.CoroutineTaskRunner
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.ui.screens.habits.edit.HabitEditorSaver
import io.github.crisacm.salem.core.ui.screens.habits.list.HabitCardListCache
import io.github.crisacm.salem.core.ui.screens.habits.list.HintList
import io.github.crisacm.salem.core.ui.screens.habits.list.HintListFactory
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsMenuBehavior
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior
import io.github.crisacm.salem.core.utils.MidnightTimer
import io.github.crisacm.salem.platform.io.AppGroup
import io.github.crisacm.salem.platform.io.AppleFileOpener
import io.github.crisacm.salem.platform.io.SqliterDatabaseOpener
import io.github.crisacm.salem.platform.time.computeToday
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.platform.time.setFirstWeekday
import io.github.crisacm.salem.platform.time.setToday
import io.github.crisacm.salem.ui.ScreenNavigation
import io.github.crisacm.salem.ui.compose.TransientMessage
import io.github.crisacm.salem.ui.detail.DetailNavigation
import io.github.crisacm.salem.ui.exporting.DatabaseBackupExport
import io.github.crisacm.salem.ui.importing.ImportFlow
import io.github.crisacm.salem.ui.list.HabitListController
import io.github.crisacm.salem.ui.list.HabitListState
import io.github.crisacm.salem.ui.list.ListHabitsMenu
import io.github.crisacm.salem.ui.list.ListHabitsSelectionMenu
import io.github.crisacm.salem.ui.list.ListOverlays
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.could_not_export
import io.github.crisacm.salem.ui.resources.hint_drag
import io.github.crisacm.salem.ui.resources.hint_landscape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.jetbrains.compose.resources.getString

/**
 * Everything the iOS app needs to show the habit list, built by hand (kotlin-inject only runs for Android): the
 * database in the files of the app with the schema of the core, the habits, the commands, and the state holders
 * of the list. Create it once with [create], off the main thread.
 */
class IosHabitsGraph private constructor(
    val preferences: Preferences,
    val habitList: HabitList,
    val logging: Logging,
    val taskRunner: TaskRunner,
    val commandRunner: CommandRunner,
    val dirFinder: IosDirFinder,
    val navigation: DetailNavigation,
    val screens: ScreenNavigation,
    val midnightTimer: MidnightTimer,
    val reminders: IosReminders,
    val importFlow: ImportFlow,
    val backupExport: DatabaseBackupExport,
    val message: TransientMessage,
    val themeSwitcher: IosThemeSwitcher,
    val screen: IosListScreen,
    val overlays: ListOverlays,
    val saverFactory: () -> HabitEditorSaver,
    val hintList: HintList,
    val listState: HabitListState,
    val menu: ListHabitsMenu,
    val selectionMenu: ListHabitsSelectionMenu,
    val controller: HabitListController,
    val behavior: ListHabitsBehavior,
    private val sqlHabitList: SQLiteHabitList,
    private val externalChanges: ExternalChanges
) {
    /**
     * Reads the habits again if the widget or a shortcut changed the database since the last time (the app does not
     * hear about those changes). The habits are new objects, so the screens that hold one are closed or opened again
     * with the new one. Call it when the app comes back to the foreground.
     */
    fun reloadIfChangedElsewhere() {
        if (!externalChanges.hasChanged()) return
        val openHabitId = navigation.habit?.id
        sqlHabitList.reload()
        for (habit in habitList) habit.recompute()
        overlays.current?.let { overlays.dismiss(it) }
        navigation.close()
        openHabitId?.let { habitList.getById(it) }?.let { navigation.open(it) }
        listState.refresh()
        reminders.rescheduleAll()
    }

    companion object {
        suspend fun create(onHabitsChanged: () -> Unit = {}): IosHabitsGraph {
            // The database and the preferences live in the folder shared with the widget and the shortcuts when this build
            // can use the App Group; otherwise, as before, in Application Support and the standard defaults
            val legacyFiles = AppleFileOpener.createDefault()
            val fileOpener = AppleFileOpener.createDefault(APP_GROUP_ID)
            if (fileOpener.userDir != legacyFiles.userDir) {
                moveDatabase(legacyFiles.openUserFile(DATABASE_FILENAME), fileOpener.openUserFile(DATABASE_FILENAME))
            }
            val preferences = Preferences(UserDefaultsStorage(AppGroup.userDefaults(APP_GROUP_ID)))
            setToday(computeToday(preferences.midnightDelayHours, 0))
            setFirstWeekday(preferences.firstWeekday)

            val database = SqliterDatabaseOpener().open(fileOpener.openUserFile(DATABASE_FILENAME).pathString)
            database.initializeHabitsSchema { version ->
                val name = version.toString().padStart(2, '0')
                fileOpener.openResourceFile("migrations/$name.sql").lines().joinToString("\n")
            }
            val modelFactory = SQLModelFactory(database)
            val habitList = modelFactory.buildHabitList()
            seedDebugHabitsIfRequested(modelFactory, habitList)
            for (habit in habitList) habit.recompute()

            val logging = StandardLogging()
            val taskRunner = CoroutineTaskRunner(Dispatchers.Main, Dispatchers.IO)
            val commandRunner = CommandRunner(taskRunner)
            // The widget draws the habits of the database: it has to hear about every change
            commandRunner.addListener(object : CommandRunner.Listener {
                override fun onCommandFinished(command: Command) = onHabitsChanged()
            })
            val cache = HabitCardListCache(habitList, commandRunner, taskRunner, logging)
            // The listeners of the timer write the state of the screen, so they run on the main thread
            val midnightTimer = MidnightTimer(logging, preferences, Dispatchers.Main)
            val themeSwitcher = IosThemeSwitcher(preferences)
            val overlays = ListOverlays()
            val navigation = DetailNavigation()
            val screens = ScreenNavigation()
            val dirFinder = IosDirFinder()
            val message = TransientMessage()
            val screen = IosListScreen(
                logging.getLogger("ListScreen"),
                themeSwitcher,
                overlays,
                navigation,
                screens,
                ::openUrl,
                IosShareSheet::shareFile,
                IosShareSheet::shareText,
                message
            )
            val behavior = ListHabitsBehavior(
                habitList,
                dirFinder,
                taskRunner,
                screen,
                commandRunner,
                preferences,
                IosBugReporter()
            )

            val listState = HabitListState(cache, preferences, midnightTimer)
            screen.onRepaired = listState::refresh
            val menuBehavior = ListHabitsMenuBehavior(screen, listState, preferences, themeSwitcher)
            val menu = ListHabitsMenu(preferences, themeSwitcher, menuBehavior)
            val selectionBehavior = ListHabitsSelectionMenuBehavior(habitList, screen, listState, commandRunner)
            lateinit var selectionMenu: ListHabitsSelectionMenu
            val controller = HabitListController(listState, behavior, lazy { selectionMenu })
            val reminders = IosReminders.create(habitList, commandRunner, taskRunner, preferences, midnightTimer, navigation)
            selectionMenu = ListHabitsSelectionMenu(
                listState,
                commandRunner,
                preferences,
                selectionBehavior,
                lazy { controller },
                reminders.notificationTray
            )

            val importFlow = IosImport.create(
                modelFactory,
                habitList,
                fileOpener,
                commandRunner,
                logging,
                refresh = {
                    // The importers write without a command: the list, the reminders and the permission follow by hand
                    listState.refresh()
                    reminders.rescheduleAll()
                    reminders.permission.onLaunch(reminders.reminderScheduler.hasHabitsWithReminders())
                    onHabitsChanged()
                },
                message = message
            )

            val backupExport = DatabaseBackupExport(
                source = fileOpener.openUserFile(DATABASE_FILENAME),
                folder = IosExports::freshFolder,
                today = ::getToday,
                share = IosShareSheet::shareFile,
                onFailed = { message.show(Res.string.could_not_export) }
            )

            // The string-array of Android refers to other strings, which Compose resources do not resolve
            val hints = arrayOf(getString(Res.string.hint_drag), getString(Res.string.hint_landscape))
            val hintList = HintListFactory(preferences).create(hints)

            return IosHabitsGraph(
                preferences, habitList, logging, taskRunner, commandRunner, dirFinder, navigation, screens, midnightTimer, reminders, importFlow, backupExport, message,
                themeSwitcher, screen, overlays,
                { HabitEditorSaver(modelFactory, habitList, commandRunner) }, hintList,
                listState, menu, selectionMenu, controller, behavior, habitList, ExternalChanges(database)
            )
        }
    }
}
