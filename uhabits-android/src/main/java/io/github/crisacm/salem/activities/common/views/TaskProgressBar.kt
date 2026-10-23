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

package io.github.crisacm.salem.activities.common.views

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import io.github.crisacm.salem.core.tasks.Task
import io.github.crisacm.salem.core.tasks.TaskRunner

class TaskProgressBar(
    context: Context,
    private val runner: TaskRunner
) : ProgressBar(
    context,
    null,
    android.R.attr.progressBarStyleHorizontal
),
    TaskRunner.Listener {

    init {
        visibility = View.GONE
        isIndeterminate = true
    }

    override fun onTaskStarted(task: Task) = update()
    override fun onTaskFinished(task: Task) = update()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        runner.addListener(this)
        update()
    }

    override fun onDetachedFromWindow() {
        runner.removeListener(this)
        super.onDetachedFromWindow()
    }

    fun update() {
        val callback = {
            val newVisibility = when (runner.activeTaskCount) {
                0 -> GONE
                else -> VISIBLE
            }
            if (visibility != newVisibility) visibility = newVisibility
        }
        postDelayed(callback, 500)
    }
}
