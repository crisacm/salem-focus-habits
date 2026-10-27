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

import io.github.crisacm.salem.core.APP_GROUP_ID
import io.github.crisacm.salem.core.DATABASE_FILENAME
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.database.initializeHabitsSchema
import io.github.crisacm.salem.core.io.GenericImporter
import io.github.crisacm.salem.core.io.HabitBullCSVImporter
import io.github.crisacm.salem.core.io.ImportResult
import io.github.crisacm.salem.core.io.ImportRunner
import io.github.crisacm.salem.core.io.LoopDBImporter
import io.github.crisacm.salem.core.io.RewireDBImporter
import io.github.crisacm.salem.core.io.StandardLogging
import io.github.crisacm.salem.core.io.TickmateDBImporter
import io.github.crisacm.salem.core.io.moveDatabase
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.ModelFactory
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.sqlite.SQLModelFactory
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.preferences.UserDefaultsStorage
import io.github.crisacm.salem.core.tasks.CoroutineTaskRunner
import io.github.crisacm.salem.core.test.HabitFixtures
import io.github.crisacm.salem.platform.io.AppGroup
import io.github.crisacm.salem.platform.io.AppleFileOpener
import io.github.crisacm.salem.platform.io.AppleUserFile
import io.github.crisacm.salem.platform.io.SqliterDatabaseOpener
import io.github.crisacm.salem.platform.time.AppleLocalDateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSProcessInfo
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * Opens the shared database and the session the SwiftUI app keeps for its lifetime.
 * Call it once, off a tight loop: it migrates the database.
 */
object SalemSessionFactory {
    @Throws(Exception::class)
    fun open(defaultBody: String, yes: String, no: String, onHabitsChanged: () -> Unit): SalemSession = runBlocking {
        val legacyFiles = AppleFileOpener.createDefault()
        val fileOpener = AppleFileOpener.createDefault(APP_GROUP_ID)
        if (fileOpener.userDir != legacyFiles.userDir) {
            moveDatabase(legacyFiles.openUserFile(DATABASE_FILENAME), fileOpener.openUserFile(DATABASE_FILENAME))
        }
        val preferences = Preferences(UserDefaultsStorage(AppGroup.userDefaults(APP_GROUP_ID)))
        val databaseFile = fileOpener.openUserFile(DATABASE_FILENAME)
        val database = SqliterDatabaseOpener().open(databaseFile.pathString)
        database.initializeHabitsSchema { version ->
            val name = version.toString().padStart(2, '0')
            fileOpener.openResourceFile("migrations/$name.sql").lines().joinToString("\n")
        }
        val modelFactory = SQLModelFactory(database)
        val habitList = modelFactory.buildHabitList()
        openHabits(preferences, habitList) { seedIfRequested(modelFactory, habitList) }

        val logging = StandardLogging()
        val taskRunner = CoroutineTaskRunner(Dispatchers.Main, Dispatchers.Default)
        val commandRunner = CommandRunner(taskRunner)
        val session = SalemSession(
            habitList = habitList,
            modelFactory = modelFactory,
            commandRunner = commandRunner,
            preferences = preferences,
            database = database,
            dateFormatter = AppleLocalDateFormatter(),
            onHabitsChanged = onHabitsChanged
        )
        val reminders = KitReminders.start(
            habitList = habitList,
            commandRunner = commandRunner,
            taskRunner = taskRunner,
            preferences = preferences,
            logging = logging,
            defaultBody = defaultBody,
            yes = yes,
            no = no,
            onOpenHabit = { id ->
                session.pendingHabitId = id
                onHabitsChanged()
            }
        )
        val importer = GenericImporter(
            LoopDBImporter(habitList, modelFactory, SqliterDatabaseOpener(), commandRunner, logging, fileOpener),
            RewireDBImporter(habitList, modelFactory, SqliterDatabaseOpener()),
            TickmateDBImporter(habitList, modelFactory, SqliterDatabaseOpener()),
            HabitBullCSVImporter(habitList, modelFactory, logging)
        )
        val runner = ImportRunner(importer, modelFactory.database) { habitList.reload() }
        session.hooks = SessionHooks(
            databasePath = databaseFile.pathString,
            reschedule = reminders::rescheduleAll,
            onLaunch = reminders.refresher::onLaunch,
            onForeground = reminders.refresher::onForeground,
            onBackground = reminders.refresher::onBackground,
            hasReminders = reminders.scheduler::hasHabitsWithReminders,
            importFile = { path ->
                val result = runBlocking { runner.run(AppleUserFile(path)) }
                when (result) {
                    ImportResult.SUCCESS -> {
                        for (habit in habitList) habit.recompute()
                        reminders.rescheduleAll()
                        SessionHooks.IMPORT_OK
                    }
                    ImportResult.NOT_RECOGNIZED -> SessionHooks.IMPORT_UNRECOGNIZED
                    ImportResult.FAILED -> SessionHooks.IMPORT_FAILED
                }
            }
        )
        KitNotificationRouter.install()
        KitNotificationRouter.attach(reminders.actions)
        session
    }
}

@OptIn(ExperimentalNativeApi::class)
private fun seedIfRequested(modelFactory: ModelFactory, habitList: HabitList) {
    if (!Platform.isDebugBinary) return
    if (NSProcessInfo.processInfo.environment["SALEM_SEED_DEBUG_DATA"] != "1") return
    if (habitList.count() > 0) return
    val fixtures = HabitFixtures(modelFactory, habitList)
    fixtures.createLongHabit().also { it.name = "Meditate" }
    fixtures.createNumericalHabit()
    fixtures.createShortHabit().also {
        it.name = "Wake up early"
        it.color = PaletteColor(8)
    }
    fixtures.createEmptyHabit("Read a book", PaletteColor(11), 3)
    habitList.update(habitList.toList())
}
