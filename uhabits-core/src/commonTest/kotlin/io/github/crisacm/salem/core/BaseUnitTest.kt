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
package io.github.crisacm.salem.core

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import io.github.crisacm.salem.platform.io.Database
import io.github.crisacm.salem.platform.io.DatabaseOpener
import io.github.crisacm.salem.platform.io.FileOpener
import io.github.crisacm.salem.platform.io.TestDatabaseHelper
import io.github.crisacm.salem.platform.io.UserFile
import io.github.crisacm.salem.platform.io.createTestDatabaseOpener
import io.github.crisacm.salem.platform.io.createTestFileOpener
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.setToday
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.ModelFactory
import io.github.crisacm.salem.core.models.memory.MemoryModelFactory
import io.github.crisacm.salem.core.tasks.CoroutineTaskRunner
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.test.HabitFixtures
import kotlin.test.BeforeTest

open class BaseUnitTest {
    protected open lateinit var habitList: HabitList
    protected lateinit var fixtures: HabitFixtures
    protected lateinit var modelFactory: ModelFactory
    protected lateinit var taskRunner: TaskRunner
    protected open lateinit var commandRunner: CommandRunner
    protected val fileOpener: FileOpener by lazy { createTestFileOpener() }
    private var _databaseOpener: DatabaseOpener? = null
    protected suspend fun databaseOpener(): DatabaseOpener {
        if (_databaseOpener == null) {
            _databaseOpener = createTestDatabaseOpener()
        }
        return _databaseOpener!!
    }

    @BeforeTest
    open fun setUp() {
        setToday(LocalDate(2015, 1, 25))
        val memoryModelFactory = MemoryModelFactory()
        habitList = memoryModelFactory.buildHabitList()
        fixtures = HabitFixtures(memoryModelFactory, habitList)
        modelFactory = memoryModelFactory
        taskRunner = CoroutineTaskRunner(
            mainDispatcher = UnconfinedTestDispatcher(),
            ioDispatcher = UnconfinedTestDispatcher()
        )
        commandRunner = CommandRunner(taskRunner)
    }

    protected suspend fun createTempDir(): UserFile {
        val dir = fileOpener.openUserFile("test-temp-dir-${tempFileCounter++}")
        dir.mkdirs()
        return dir
    }

    protected suspend fun copyResourceToTempFile(resourcePath: String): UserFile {
        val cleanPath = resourcePath.removePrefix("/")
        val tempFile = fileOpener.openUserFile("test-temp-${tempFileCounter++}")
        fileOpener.openResourceFile(cleanPath).copyTo(tempFile)
        return tempFile
    }

    protected suspend fun openDatabaseResource(resourcePath: String): Database {
        val tempFile = copyResourceToTempFile(resourcePath)
        return databaseOpener().open(tempFile.pathString)
    }

    companion object {
        private var tempFileCounter = 0

        suspend fun buildMemoryDatabase(): Database {
            return TestDatabaseHelper.createEmptyDatabase()
        }
    }
}
