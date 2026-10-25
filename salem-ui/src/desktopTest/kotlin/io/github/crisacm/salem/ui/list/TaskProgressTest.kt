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

package io.github.crisacm.salem.ui.list

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import io.github.crisacm.salem.core.tasks.Task
import io.github.crisacm.salem.core.tasks.TaskRunner
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TaskProgressTest {
    private class FakeRunner : TaskRunner {
        val listeners = mutableListOf<TaskRunner.Listener>()
        override var activeTaskCount = 0

        override fun addListener(listener: TaskRunner.Listener) {
            listeners += listener
        }

        override fun removeListener(listener: TaskRunner.Listener) {
            listeners -= listener
        }

        override fun execute(task: Task) {}

        override fun publishProgress(task: Task, progress: Int) {}

        override suspend fun await() {}

        fun start(task: Task) {
            activeTaskCount++
            listeners.toList().forEach { it.onTaskStarted(task) }
        }

        fun finish(task: Task) {
            activeTaskCount--
            listeners.toList().forEach { it.onTaskFinished(task) }
        }
    }

    @Test
    fun showsHalfASecondAfterATaskStartsAndHidesAfterItEnds() = runComposeUiTest {
        // The bar is indeterminate: it animates forever, so the clock is advanced by hand
        mainClock.autoAdvance = false
        val runner = FakeRunner()
        val task = Task { }
        setContent { MaterialTheme { TaskProgress(runner) } }
        mainClock.advanceTimeBy(100)
        onNodeWithTag("taskProgress").assertDoesNotExist()

        runner.start(task)
        mainClock.advanceTimeBy(100)
        onNodeWithTag("taskProgress").assertDoesNotExist()
        mainClock.advanceTimeBy(600)
        onNodeWithTag("taskProgress").assertExists()

        runner.finish(task)
        mainClock.advanceTimeBy(700)
        onNodeWithTag("taskProgress").assertDoesNotExist()
    }
}
