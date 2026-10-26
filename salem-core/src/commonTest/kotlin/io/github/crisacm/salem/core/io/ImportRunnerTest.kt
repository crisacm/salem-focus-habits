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

package io.github.crisacm.salem.core.io

import io.github.crisacm.salem.core.BaseUnitTest
import io.github.crisacm.salem.core.models.sqlite.SQLModelFactory
import io.github.crisacm.salem.core.models.sqlite.SQLiteHabitList
import io.github.crisacm.salem.platform.io.Database
import io.github.crisacm.salem.platform.io.UserFile
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ImportRunnerTest : BaseUnitTest() {
    private lateinit var sqlHabitList: SQLiteHabitList
    private lateinit var database: Database
    private var reloads = 0

    @BeforeTest
    override fun setUp() {
        super.setUp()
        reloads = 0
    }

    // A real SQLite database for the habits, as the apps have: the runner's transaction needs one
    private suspend fun buildImporter(): Pair<ImportRunner, GenericImporter> {
        database = buildMemoryDatabase()
        val factory = SQLModelFactory(database)
        sqlHabitList = SQLiteHabitList(factory)
        habitList = sqlHabitList
        val opener = databaseOpener()
        val importer = GenericImporter(
            LoopDBImporter(habitList, factory, opener, commandRunner, StandardLogging(), fileOpener),
            RewireDBImporter(habitList, factory, opener),
            TickmateDBImporter(habitList, factory, opener),
            HabitBullCSVImporter(habitList, factory, StandardLogging())
        )
        return ImportRunner(importer, database) { reloads++; sqlHabitList.reload() } to importer
    }

    @Test
    fun aKnownFileIsImportedAndKept() = runTest {
        val (runner, _) = buildImporter()
        val result = runner.run(copyResourceToTempFile("loop.db"))
        assertEquals(ImportResult.SUCCESS, result)
        assertEquals(9, habitList.size())
        // What was committed is what a new list reads from the database
        sqlHabitList.reload()
        assertEquals(9, habitList.size())
        assertEquals(0, reloads)
    }

    @Test
    fun aFileNobodyRecognisesLeavesTheHabitsAsTheyWere() = runTest {
        val (runner, _) = buildImporter()
        val result = runner.run(copyResourceToTempFile("hello.txt"))
        assertEquals(ImportResult.NOT_RECOGNIZED, result)
        assertEquals(0, habitList.size())
        assertEquals(0, reloads)
    }

    @Test
    fun anImportThatFailsHalfWayLeavesNothingBehind() = runTest {
        val (runner, real) = buildImporter()
        val failing = ImportRunner(
            object : AbstractImporter() {
                override suspend fun canHandle(file: UserFile) = real.canHandle(file)

                override suspend fun importHabitsFromFile(file: UserFile) {
                    real.importHabitsFromFile(file)
                    check(habitList.size() == 9) { "The habits were imported before the failure" }
                    throw IllegalStateException("The disk is full")
                }
            },
            database
        ) { reloads++; sqlHabitList.reload() }

        val result = failing.run(copyResourceToTempFile("loop.db"))

        assertEquals(ImportResult.FAILED, result)
        assertEquals(1, reloads)
        assertEquals(0, habitList.size())
    }

    @Test
    fun theRunnerCanImportAgainAfterAFailure() = runTest {
        val (runner, real) = buildImporter()
        val failing = ImportRunner(
            object : AbstractImporter() {
                override suspend fun canHandle(file: UserFile) = true
                override suspend fun importHabitsFromFile(file: UserFile) = throw IllegalStateException("No")
            },
            database
        ) { sqlHabitList.reload() }
        assertEquals(ImportResult.FAILED, failing.run(copyResourceToTempFile("loop.db")))

        assertEquals(ImportResult.SUCCESS, runner.run(copyResourceToTempFile("loop.db")))
        assertEquals(9, habitList.size())
    }
}
