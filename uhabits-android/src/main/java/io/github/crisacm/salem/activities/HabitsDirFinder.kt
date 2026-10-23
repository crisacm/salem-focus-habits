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
package io.github.crisacm.salem.activities

import me.tatarka.inject.annotations.Inject
import io.github.crisacm.salem.platform.io.JavaUserFile
import io.github.crisacm.salem.platform.io.UserFile
import io.github.crisacm.salem.AndroidDirFinder
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter

@Inject
class HabitsDirFinder(
    private val androidDirFinder: AndroidDirFinder
) : ShowHabitMenuPresenter.System, ListHabitsBehavior.DirFinder {

    override fun getCSVOutputDir(): UserFile {
        return JavaUserFile(androidDirFinder.getFilesDir("CSV")!!.toPath())
    }
}
