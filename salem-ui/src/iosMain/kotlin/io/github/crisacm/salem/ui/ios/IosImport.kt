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
import io.github.crisacm.salem.core.io.GenericImporter
import io.github.crisacm.salem.core.io.HabitBullCSVImporter
import io.github.crisacm.salem.core.io.ImportRunner
import io.github.crisacm.salem.core.io.Logging
import io.github.crisacm.salem.core.io.LoopDBImporter
import io.github.crisacm.salem.core.io.RewireDBImporter
import io.github.crisacm.salem.core.io.TickmateDBImporter
import io.github.crisacm.salem.core.models.sqlite.SQLModelFactory
import io.github.crisacm.salem.core.models.sqlite.SQLiteHabitList
import io.github.crisacm.salem.platform.io.FileOpener
import io.github.crisacm.salem.platform.io.SqliterDatabaseOpener
import io.github.crisacm.salem.ui.compose.TransientMessage
import io.github.crisacm.salem.ui.importing.ImportFlow
import io.github.crisacm.salem.ui.importing.importMessage

/**
 * The import of data (a Loop database, or the files of Rewire, Tickmate and HabitBull) of the iOS app. The importers
 * are built by hand, as everything else on iOS. They write the habits without going through the [CommandRunner], so
 * when it ends the screens are refreshed by hand: the list, the reminders and the permission to notify.
 */
object IosImport {
    fun create(
        modelFactory: SQLModelFactory,
        habitList: SQLiteHabitList,
        fileOpener: FileOpener,
        commandRunner: CommandRunner,
        logging: Logging,
        refresh: () -> Unit,
        message: TransientMessage
    ): ImportFlow {
        val opener = SqliterDatabaseOpener()
        val importer = GenericImporter(
            LoopDBImporter(habitList, modelFactory, opener, commandRunner, logging, fileOpener),
            RewireDBImporter(habitList, modelFactory, opener),
            TickmateDBImporter(habitList, modelFactory, opener),
            HabitBullCSVImporter(habitList, modelFactory, logging)
        )
        val runner = ImportRunner(importer, modelFactory.database) { habitList.reload() }
        return ImportFlow(
            pickFile = IosFilePicker::pickFile,
            runner = runner::run,
            refresh = refresh,
            onFinished = { message.show(importMessage(it)) }
        )
    }
}
