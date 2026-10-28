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

import io.github.crisacm.salem.core.DATABASE_VERSION
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.sqlite.SQLModelFactory
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.tasks.Task
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.test.HabitFixtures
import io.github.crisacm.salem.platform.gui.DrawOp
import io.github.crisacm.salem.platform.io.JavaDatabaseOpener
import io.github.crisacm.salem.platform.io.JavaResourceFile
import io.github.crisacm.salem.platform.io.migrateTo
import io.github.crisacm.salem.platform.io.setVersion
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.computeToday
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.platform.time.resetToday
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SalemSessionTest {
    private val today = LocalDate(2026, 10, 26)
    private lateinit var session: SalemSession
    private lateinit var fixtures: HabitFixtures
    private lateinit var habits: HabitList

    @Before
    fun setUp() {
        val database = JavaDatabaseOpener().open(":memory:")
        runBlocking {
            database.setVersion(8)
            database.migrateTo(DATABASE_VERSION) { JavaResourceFile("migrations/%02d.sql".format(it)).lines().joinToString("\n") }
        }
        val factory = SQLModelFactory(database)
        habits = factory.buildHabitList()
        fixtures = HabitFixtures(factory, habits)
        session = SalemSession(
            habitList = habits,
            modelFactory = factory,
            commandRunner = CommandRunner(InlineTaskRunner()),
            preferences = Preferences(MemoryStorage()),
            database = database,
            clock = { today }
        )
    }

    @After
    fun tearDown() {
        session.close()
        resetToday()
    }

    @Test
    fun opensAnEmptyDatabase() {
        assertTrue(session.listSnapshot(7).rows.isEmpty())
    }

    @Test
    fun openHabitsSetsTodayBeforeRecompute() {
        fixtures.createEmptyHabit()
        resetToday()
        val preferences = Preferences(MemoryStorage())
        openHabits(preferences, habits)
        assertEquals(computeToday(0, 0), getToday())
    }

    @Test
    fun toggleMarksTodayAndKeepsIt() {
        val habit = fixtures.createEmptyHabit()
        session.toggle(habit.id!!, today.daysSince2000)
        val cell = session.listSnapshot(1).rows.single().cells.single()
        assertEquals(Entry.YES_MANUAL, cell.value)
    }

    @Test
    fun toggleUnchecksAfterSecondTap() {
        val habit = fixtures.createEmptyHabit()
        val day = today.daysSince2000
        session.toggle(habit.id!!, day)
        session.toggle(habit.id!!, day)
        val cell = session.listSnapshot(1).rows.single().cells.single()
        assertEquals(Entry.NO, cell.value)
    }

    @Test
    fun createsEditsAndArchivesAHabit() {
        val blank = session.blankEditor(HabitType.NUMERICAL.value)
        val created = session.saveEditor(
            EditorSnapshot(
                id = blank.id,
                type = HabitType.NUMERICAL.value,
                name = "Run",
                question = "How far?",
                notes = "",
                unit = "km",
                targetText = "3",
                targetType = blank.targetType,
                color = 4,
                freqNum = 1,
                freqDen = 1,
                reminderHour = -1,
                reminderMin = -1,
                reminderDays = blank.reminderDays
            )
        )
        assertTrue(created >= 0)
        assertEquals("Run", session.listSnapshot(1).rows.single().name)
        val edited = session.editorSnapshot(created)
        session.saveEditor(
            EditorSnapshot(
                id = edited.id,
                type = edited.type,
                name = "Jog",
                question = edited.question,
                notes = edited.notes,
                unit = edited.unit,
                targetText = edited.targetText,
                targetType = edited.targetType,
                color = edited.color,
                freqNum = edited.freqNum,
                freqDen = edited.freqDen,
                reminderHour = edited.reminderHour,
                reminderMin = edited.reminderMin,
                reminderDays = edited.reminderDays
            )
        )
        assertEquals("Jog", session.detail(created)!!.name)
        session.archive(listOf(created))
        assertTrue(session.listSnapshot(1).rows.isEmpty())
        session.setShowArchived(true)
        assertEquals(created, session.listSnapshot(1).rows.single().id)
    }

    @Test
    fun toggleAndArchiveApplyWithoutQueuedTasks() {
        val database = JavaDatabaseOpener().open(":memory:")
        runBlocking {
            database.setVersion(8)
            database.migrateTo(DATABASE_VERSION) { JavaResourceFile("migrations/%02d.sql".format(it)).lines().joinToString("\n") }
        }
        val factory = SQLModelFactory(database)
        val habits = factory.buildHabitList()
        val deferred = SalemSession(
            habitList = habits,
            modelFactory = factory,
            commandRunner = CommandRunner(NoExecuteTaskRunner()),
            preferences = Preferences(MemoryStorage()),
            database = database,
            clock = { today }
        )
        try {
            val habit = HabitFixtures(factory, habits).createEmptyHabit()
            val day = today.daysSince2000
            deferred.toggle(habit.id!!, day)
            assertEquals(Entry.YES_MANUAL, deferred.listSnapshot(1).rows.single().cells.single().value)
            deferred.archive(listOf(habit.id!!))
            assertTrue(deferred.listSnapshot(1).rows.isEmpty())
        } finally {
            deferred.close()
        }
    }

    @Test
    fun saveEditorCreatesWithoutRunningQueuedTasks() {
        val database = JavaDatabaseOpener().open(":memory:")
        runBlocking {
            database.setVersion(8)
            database.migrateTo(DATABASE_VERSION) { JavaResourceFile("migrations/%02d.sql".format(it)).lines().joinToString("\n") }
        }
        val factory = SQLModelFactory(database)
        val habits = factory.buildHabitList()
        val deferred = SalemSession(
            habitList = habits,
            modelFactory = factory,
            commandRunner = CommandRunner(NoExecuteTaskRunner()),
            preferences = Preferences(MemoryStorage()),
            database = database,
            clock = { today }
        )
        try {
            val blank = deferred.blankEditor(HabitType.YES_NO.value)
            val first = deferred.saveEditor(
                EditorSnapshot(
                    id = blank.id,
                    type = blank.type,
                    name = "One",
                    question = blank.question,
                    notes = blank.notes,
                    unit = blank.unit,
                    targetText = blank.targetText,
                    targetType = blank.targetType,
                    color = blank.color,
                    freqNum = blank.freqNum,
                    freqDen = blank.freqDen,
                    reminderHour = blank.reminderHour,
                    reminderMin = blank.reminderMin,
                    reminderDays = blank.reminderDays
                )
            )
            assertTrue(first > 0)
            assertEquals(first, deferred.listSnapshot(1).rows.single().id)
            val second = deferred.saveEditor(
                EditorSnapshot(
                    id = blank.id,
                    type = blank.type,
                    name = "Two",
                    question = blank.question,
                    notes = blank.notes,
                    unit = blank.unit,
                    targetText = blank.targetText,
                    targetType = blank.targetType,
                    color = blank.color,
                    freqNum = blank.freqNum,
                    freqDen = blank.freqDen,
                    reminderHour = blank.reminderHour,
                    reminderMin = blank.reminderMin,
                    reminderDays = blank.reminderDays
                )
            )
            assertTrue(second > 0)
            assertNotEquals(first, second)
            assertEquals(2, deferred.listSnapshot(1).rows.size)
        } finally {
            deferred.close()
        }
    }

    @Test
    fun rejectsABlankName() {
        val blank = session.blankEditor(HabitType.YES_NO.value)
        assertEquals(-1, session.saveEditor(blank))
    }

    @Test
    fun rejectsAnInvalidNumericalTarget() {
        val blank = session.blankEditor(HabitType.NUMERICAL.value)
        assertEquals(
            -3,
            session.saveEditor(
                EditorSnapshot(
                    id = blank.id,
                    type = HabitType.NUMERICAL.value,
                    name = "Run",
                    question = blank.question,
                    notes = blank.notes,
                    unit = blank.unit,
                    targetText = "",
                    targetType = blank.targetType,
                    color = blank.color,
                    freqNum = blank.freqNum,
                    freqDen = blank.freqDen,
                    reminderHour = blank.reminderHour,
                    reminderMin = blank.reminderMin,
                    reminderDays = blank.reminderDays
                )
            )
        )
        val saved = session.saveEditor(
            EditorSnapshot(
                id = blank.id,
                type = HabitType.NUMERICAL.value,
                name = "Run",
                question = blank.question,
                notes = blank.notes,
                unit = blank.unit,
                targetText = "3",
                targetType = blank.targetType,
                color = blank.color,
                freqNum = blank.freqNum,
                freqDen = blank.freqDen,
                reminderHour = blank.reminderHour,
                reminderMin = blank.reminderMin,
                reminderDays = blank.reminderDays
            )
        )
        assertTrue(saved >= 0)
    }

    @Test
    fun settingsRoundTrip() {
        session.setSwitch(SettingKey.SKIP, true)
        session.setFirstWeekday(1)
        val settings = session.settingsSnapshot()
        assertTrue(settings.skip)
        assertEquals(1, settings.firstWeekday)
        assertFalse(settings.developer)
    }

    @Test
    fun historyChartRecordsPaintOperations() {
        val habit = fixtures.createLongHabit()
        val ops = session.chartOps(habit.id!!, ChartCard.HISTORY, 320.0, 160.0, 0, emptyList())
        assertTrue(ops.any { it.kind == DrawOp.FILL_RECT || it.kind == DrawOp.FILL_ROUND_RECT })
    }

    @Test
    fun detailChartsGiveTheDataOfTheCards() {
        val habit = fixtures.createLongHabit()
        val charts = session.detailCharts(habit.id!!)!!
        assertEquals(today.daysSince2000, charts.today)
        assertEquals(7, charts.weekdays.size)
        assertEquals((0 until 7).toList(), charts.weekdays.map { it.day })
        assertTrue(charts.scoresByWeek.isNotEmpty())
        assertEquals(charts.scoresByWeek.map { it.day }.sorted(), charts.scoresByWeek.map { it.day })
        assertEquals(charts.barsByMonth.map { it.day }.sorted(), charts.barsByMonth.map { it.day })
        assertTrue(charts.scoresByWeek.size >= charts.scoresByMonth.size)
        assertEquals(today.daysSince2000, charts.calendar.first().day)
        assertTrue(charts.streaks.all { it.length == it.end - it.start + 1 })
    }

    @Test
    fun detailChartsOfAMissingHabitAreNull() {
        assertEquals(null, session.detailCharts(12345))
    }
}

/** Queues tasks like production runners but never executes them (toggle still needs [InlineTaskRunner]). */
private class NoExecuteTaskRunner : TaskRunner {
    override fun addListener(listener: TaskRunner.Listener) {}
    override fun removeListener(listener: TaskRunner.Listener) {}
    override fun execute(task: Task) {}
    override fun publishProgress(task: Task, progress: Int) {}
    override val activeTaskCount = 0
    override suspend fun await() {}
}

private class InlineTaskRunner : TaskRunner {
    override fun addListener(listener: TaskRunner.Listener) {}
    override fun removeListener(listener: TaskRunner.Listener) {}
    override fun execute(task: Task) {
        task.onPreExecute()
        runBlocking { task.doInBackground() }
        task.onPostExecute()
    }
    override fun publishProgress(task: Task, progress: Int) {}
    override val activeTaskCount = 0
    override suspend fun await() {}
}
