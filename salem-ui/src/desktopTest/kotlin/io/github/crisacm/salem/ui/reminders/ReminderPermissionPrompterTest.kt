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

package io.github.crisacm.salem.ui.reminders

import io.github.crisacm.salem.core.commands.ArchiveHabitsCommand
import io.github.crisacm.salem.core.commands.CreateHabitCommand
import io.github.crisacm.salem.core.commands.EditHabitCommand
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.Reminder
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.core.models.memory.MemoryModelFactory
import io.github.crisacm.salem.ui.BaseUiTest
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ReminderPermissionPrompterTest : BaseUiTest() {
    private var requests = 0
    private var failure: Exception? = null
    private lateinit var prompter: ReminderPermissionPrompter
    private lateinit var habit: Habit

    @Before
    override fun setUp() {
        super.setUp()
        requests = 0
        failure = null
        prompter = ReminderPermissionPrompter(
            request = {
                requests++
                failure?.let { throw it }
                true
            },
            scope = TestScope(UnconfinedTestDispatcher())
        )
        habit = fixtures.createEmptyHabit().also { habitList.add(it) }
    }

    private fun withReminder() = fixtures.createEmptyHabit().also {
        it.reminder = Reminder(8, 0, WeekdayList.EVERY_DAY)
    }

    @Test
    fun creatingAHabitWithAReminderAsks() {
        prompter.onCommandFinished(CreateHabitCommand(MemoryModelFactory(), habitList, withReminder()))
        assertEquals(1, requests)
    }

    @Test
    fun creatingAHabitWithoutAReminderDoesNotAsk() {
        prompter.onCommandFinished(CreateHabitCommand(MemoryModelFactory(), habitList, fixtures.createEmptyHabit()))
        assertEquals(0, requests)
    }

    @Test
    fun editingAHabitToHaveAReminderAsks() {
        prompter.onCommandFinished(EditHabitCommand(habitList, habit.id!!, withReminder()))
        assertEquals(1, requests)
    }

    @Test
    fun editingAHabitWithoutAReminderDoesNotAsk() {
        prompter.onCommandFinished(EditHabitCommand(habitList, habit.id!!, fixtures.createEmptyHabit()))
        assertEquals(0, requests)
    }

    @Test
    fun otherCommandsDoNotAsk() {
        prompter.onCommandFinished(ArchiveHabitsCommand(habitList, listOf(habit)))
        assertEquals(0, requests)
    }

    @Test
    fun launchingAsksOnlyWhenThereAreReminders() {
        prompter.onLaunch(hasReminders = false)
        assertEquals(0, requests)
        prompter.onLaunch(hasReminders = true)
        assertEquals(1, requests)
    }

    @Test
    fun aFailingRequestDoesNotBreakTheCommands() {
        failure = IllegalStateException("no notification center")
        prompter.onCommandFinished(CreateHabitCommand(MemoryModelFactory(), habitList, withReminder()))
        assertEquals(1, requests)
        // Still working afterwards
        failure = null
        prompter.onCommandFinished(CreateHabitCommand(MemoryModelFactory(), habitList, withReminder()))
        assertEquals(2, requests)
    }
}
