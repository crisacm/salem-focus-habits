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

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.crisacm.salem.core.tasks.Task
import io.github.crisacm.salem.core.tasks.TaskRunner
import kotlinx.coroutines.delay

private const val SHOW_DELAY_MILLIS = 500L

/** An indeterminate bar that shows up while [runner] has tasks running, half a second after the count changes. */
@Composable
fun TaskProgress(runner: TaskRunner, modifier: Modifier = Modifier) {
    var active by remember(runner) { mutableIntStateOf(runner.activeTaskCount) }
    var visible by remember(runner) { mutableStateOf(false) }
    DisposableEffect(runner) {
        val listener = object : TaskRunner.Listener {
            override fun onTaskStarted(task: Task) {
                active = runner.activeTaskCount
            }

            override fun onTaskFinished(task: Task) {
                active = runner.activeTaskCount
            }
        }
        runner.addListener(listener)
        active = runner.activeTaskCount
        onDispose { runner.removeListener(listener) }
    }
    // The bar follows the last count, so a short task does not make it flash
    LaunchedEffect(active) {
        delay(SHOW_DELAY_MILLIS)
        visible = active > 0
    }
    if (visible) LinearProgressIndicator(modifier = modifier.testTag("taskProgress"))
}
