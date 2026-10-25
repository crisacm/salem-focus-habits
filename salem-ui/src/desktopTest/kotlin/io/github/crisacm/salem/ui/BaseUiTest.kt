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
package io.github.crisacm.salem.ui

import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.memory.MemoryModelFactory
import io.github.crisacm.salem.core.tasks.CoroutineTaskRunner
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.test.HabitFixtures
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.resetToday
import io.github.crisacm.salem.platform.time.setToday
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before

open class BaseUiTest {
    protected lateinit var habitList: HabitList
    protected lateinit var fixtures: HabitFixtures
    private lateinit var modelFactory: MemoryModelFactory
    protected lateinit var taskRunner: TaskRunner
    protected lateinit var commandRunner: CommandRunner

    @Before
    open fun setUp() {
        setToday(LocalDate(2015, 1, 25))
        modelFactory = MemoryModelFactory()
        habitList = modelFactory.buildHabitList()
        fixtures = HabitFixtures(modelFactory, habitList)
        taskRunner = CoroutineTaskRunner(
            mainDispatcher = UnconfinedTestDispatcher(),
            ioDispatcher = UnconfinedTestDispatcher()
        )
        commandRunner = CommandRunner(taskRunner)
    }

    @After
    fun tearDown() = resetToday()
}
