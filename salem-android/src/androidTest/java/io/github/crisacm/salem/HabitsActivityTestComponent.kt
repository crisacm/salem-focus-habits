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

package io.github.crisacm.salem

import android.content.Context
import dev.mokkery.mock
import io.github.crisacm.salem.activities.HabitsDirFinder
import io.github.crisacm.salem.activities.habits.list.ListHabitsModule
import io.github.crisacm.salem.activities.habits.list.ListHabitsScreen
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsMenuBehavior
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior
import io.github.crisacm.salem.inject.ActivityContext
import io.github.crisacm.salem.inject.ActivityScope
import io.github.crisacm.salem.inject.HabitsApplicationComponent
import io.github.crisacm.salem.ui.list.HabitListState
import io.github.crisacm.salem.ui.list.ListHabitsSelectionMenu
import io.github.crisacm.salem.ui.list.SelectionListener
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@ActivityScope
@Component
abstract class HabitsActivityTestComponent(
    @Component val parent: HabitsApplicationComponent,
    @get:Provides @get:ActivityContext
    val activityContext: Context
) {
    @Provides
    open fun listHabitsBehavior(): ListHabitsBehavior = mock()

    open val HabitListState.bindAdapter: ListHabitsMenuBehavior.Adapter
        @Provides get() = this

    open val ListHabitsModule.bindBugReporter: ListHabitsBehavior.BugReporter
        @Provides get() = this

    open val ListHabitsScreen.bindMenuScreen: ListHabitsMenuBehavior.Screen
        @Provides get() = this

    open val ListHabitsScreen.bindScreen: ListHabitsBehavior.Screen
        @Provides get() = this

    open val HabitListState.bindSelMenuAdapter: ListHabitsSelectionMenuBehavior.Adapter
        @Provides get() = this

    open val ListHabitsSelectionMenu.bindSelectionListener: SelectionListener
        @Provides get() = this

    open val ListHabitsScreen.bindSelMenuScreen: ListHabitsSelectionMenuBehavior.Screen
        @Provides get() = this

    open val HabitsDirFinder.bindDirFinder: ListHabitsBehavior.DirFinder
        @Provides get() = this
}
