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

package io.github.crisacm.salem.extras

import io.github.crisacm.salem.core.DATABASE_VERSION
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.sqlite.SQLModelFactory
import io.github.crisacm.salem.core.test.HabitFixtures
import io.github.crisacm.salem.platform.io.Database
import io.github.crisacm.salem.platform.io.JavaDatabaseOpener
import io.github.crisacm.salem.platform.io.JavaResourceFile
import io.github.crisacm.salem.platform.io.migrateTo
import io.github.crisacm.salem.platform.io.setVersion
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.resetToday
import io.github.crisacm.salem.platform.time.setToday
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitsBoardTest {
    private val today = LocalDate(2026, 10, 26)
    private lateinit var database: Database
    private lateinit var factory: SQLModelFactory
    private lateinit var habits: HabitList
    private lateinit var fixtures: HabitFixtures
    private val changed = mutableListOf<Long>()

    @Before
    fun setUp() {
        setToday(today)
        // A file database, like the apps and the extensions share
        val file = Files.createTempFile("board", ".db").toString()
        database = JavaDatabaseOpener().open(file)
        runBlocking {
            database.setVersion(8)
            database.migrateTo(DATABASE_VERSION) { JavaResourceFile("migrations/%02d.sql".format(it)).lines().joinToString("\n") }
        }
        factory = SQLModelFactory(database)
        habits = factory.buildHabitList()
        fixtures = HabitFixtures(factory, habits)
    }

    @After
    fun tearDown() = resetToday()

    private fun board(skip: Boolean = false, questionMarks: Boolean = false) = HabitsBoard(
        database = database,
        date = { today },
        isSkipEnabled = skip,
        areQuestionMarksEnabled = questionMarks,
        onChanged = { changed += it }
    )

    // The fixtures of an SQL list save the habit, which gives it its id
    private fun yesNo(name: String = "Meditate"): Habit = fixtures.createEmptyHabit(name = name)

    private fun numerical(target: Double = 3.0): Habit =
        fixtures.createEmptyHabit(name = "Run").also {
            it.type = HabitType.NUMERICAL
            it.targetType = NumericalHabitType.AT_LEAST
            it.targetValue = target
            it.unit = "km"
            habits.update(it)
        }

    private fun valueOnDisk(id: Long): Int {
        // A new board reads the database again, as the next widget refresh would
        return board().today().habits.single { it.id == id }.value
    }

    @Test
    fun listsTheActiveHabitsInTheOrderOfTheApp() {
        // The list gives each habit the next position when it is created
        yesNo("First")
        yesNo("Second")
        val archived = yesNo("Archived")
        archived.isArchived = true
        habits.update(archived)

        val state = board().today()

        assertFalse(state.needsApp)
        assertEquals(listOf("First", "Second"), state.habits.map { it.name })
    }

    @Test
    fun anEmptyDatabaseHasNoHabits() {
        val state = board().today()
        assertFalse(state.needsApp)
        assertTrue(state.habits.isEmpty())
    }

    @Test
    fun aDatabaseOlderThanTheAppAsksToOpenTheApp() {
        yesNo()
        database.setVersion(DATABASE_VERSION - 1)
        val state = board().today()
        assertTrue(state.needsApp)
        assertTrue(state.habits.isEmpty())
        assertNull(board().check(1))
    }

    @Test
    fun showsTheColorOfTheHabit() {
        val habit = fixtures.createEmptyHabit(color = PaletteColor(3))
        val shown = board().today().habits.single { it.id == habit.id }
        // Palette 3 of the light theme (0xFF8F00) with full opacity
        assertEquals(0xFFFF8F00, shown.colorArgb)
    }

    @Test
    fun checkingMarksTodayAndIsKept() {
        val habit = yesNo()
        val after = assertNotNull(board().check(habit.id!!))
        assertTrue(after.isDone)
        assertEquals(Entry.YES_MANUAL, after.value)
        assertEquals(Entry.YES_MANUAL, valueOnDisk(habit.id!!))
        assertEquals(listOf(habit.id!!), changed)
    }

    @Test
    fun uncheckingMarksTodayAsNotDone() {
        val habit = yesNo()
        board().check(habit.id!!)
        val after = assertNotNull(board().uncheck(habit.id!!))
        assertFalse(after.isDone)
        assertEquals(Entry.NO, after.value)
    }

    @Test
    fun togglingFollowsTheCycleOfTheApp() {
        val habit = yesNo()
        assertEquals(Entry.YES_MANUAL, board().toggle(habit.id!!)?.value)
        assertEquals(Entry.NO, board().toggle(habit.id!!)?.value)
        assertEquals(Entry.YES_MANUAL, board().toggle(habit.id!!)?.value)
    }

    @Test
    fun togglingKnowsAboutSkipDaysAndQuestionMarks() {
        val habit = yesNo()
        val id = habit.id!!
        assertEquals(Entry.YES_MANUAL, board(skip = true, questionMarks = true).toggle(id)?.value)
        assertEquals(Entry.SKIP, board(skip = true, questionMarks = true).toggle(id)?.value)
        assertEquals(Entry.NO, board(skip = true, questionMarks = true).toggle(id)?.value)
        assertEquals(Entry.UNKNOWN, board(skip = true, questionMarks = true).toggle(id)?.value)
    }

    @Test
    fun incrementingAYesNoHabitChecksItAndDecrementingUnchecksIt() {
        val habit = yesNo()
        assertTrue(assertNotNull(board().increment(habit.id!!)).isDone)
        assertFalse(assertNotNull(board().decrement(habit.id!!)).isDone)
    }

    @Test
    fun aNumericalHabitIsIncrementedByOneUnitAndNeverBelowZero() {
        val habit = numerical()
        val id = habit.id!!
        assertEquals(1.0, board().increment(id)?.number)
        assertEquals(2.0, board().increment(id)?.number)
        assertEquals(1.0, board().decrement(id)?.number)
        assertEquals(0.0, board().decrement(id)?.number)
        assertEquals(0.0, board().decrement(id)?.number)
    }

    @Test
    fun checkingANumericalHabitReachesItsTargetAndUncheckingClearsIt() {
        val habit = numerical(target = 3.0)
        val id = habit.id!!
        val checked = assertNotNull(board().check(id))
        assertEquals(3.0, checked.number)
        assertTrue(checked.isDone)
        assertEquals(3.0, checked.target)
        assertEquals("km", checked.unit)
        val unchecked = assertNotNull(board().uncheck(id))
        assertEquals(0.0, unchecked.number)
        assertFalse(unchecked.isDone)
    }

    @Test
    fun aNumericalHabitAlreadyAboveItsTargetIsNotLowered() {
        val habit = numerical(target = 3.0)
        val id = habit.id!!
        repeat(5) { board().increment(id) }
        assertEquals(5.0, board().check(id)?.number)
    }

    @Test
    fun anUnknownOrArchivedHabitChangesNothing() {
        val archived = yesNo()
        archived.isArchived = true
        habits.update(archived)
        assertNull(board().check(9999))
        assertNull(board().toggle(archived.id!!))
        assertTrue(changed.isEmpty())
        assertTrue(board().today().habits.isEmpty())
    }

    @Test
    fun theScoreGrowsWithTheChecksAndStaysBetweenZeroAndAHundred() {
        val habit = yesNo()
        val before = board().today().habits.single().scorePercent
        board().check(habit.id!!)
        val after = board().today().habits.single().scorePercent
        assertTrue(after > before, "$before -> $after")
        assertTrue(after in 0..100)
    }

    @Test
    fun theNotesOfTheDayAreKept() {
        val habit = yesNo()
        habit.originalEntries.add(Entry(today, Entry.YES_MANUAL, "felt good"))
        board().toggle(habit.id!!)
        // Read again from the database, as the app does when it comes back
        val reread = SQLModelFactory(database).buildHabitList().getById(habit.id!!)!!
        assertEquals("felt good", reread.originalEntries.get(today).notes)
        assertEquals(Entry.NO, reread.originalEntries.get(today).value)
    }

    @Test
    fun `close releases the connection so later calls fail instead of leaking it`() {
        val board = board()
        yesNo()
        assertEquals(1, board.today().habits.size)
        board.close()
        assertFailsWith<Exception> { board.today() }
    }
}
