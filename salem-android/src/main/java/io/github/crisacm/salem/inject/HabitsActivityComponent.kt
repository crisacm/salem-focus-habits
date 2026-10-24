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

package io.github.crisacm.salem.inject

import android.content.Context
import io.github.crisacm.salem.activities.AndroidThemeSwitcher
import io.github.crisacm.salem.activities.HabitsDirFinder
import io.github.crisacm.salem.activities.common.dialogs.ColorPickerDialogFactory
import io.github.crisacm.salem.activities.habits.list.HabitListState
import io.github.crisacm.salem.activities.habits.list.ListHabitsHost
import io.github.crisacm.salem.activities.habits.list.ListHabitsMenu
import io.github.crisacm.salem.activities.habits.list.ListHabitsModule
import io.github.crisacm.salem.activities.habits.list.ListHabitsScreen
import io.github.crisacm.salem.activities.habits.list.ListHabitsSelectionMenu
import io.github.crisacm.salem.activities.habits.list.SelectionListener
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsMenuBehavior
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@ActivityScope
@Component
abstract class HabitsActivityComponent(
    @Component val parent: HabitsApplicationComponent,
    @get:Provides @get:ActivityContext
    val activityContext: Context
) {
    abstract val colorPickerDialogFactory: ColorPickerDialogFactory
    abstract val habitListState: HabitListState
    abstract val listHabitsBehavior: ListHabitsBehavior
    abstract val listHabitsMenu: ListHabitsMenu
    abstract val listHabitsHost: ListHabitsHost
    abstract val listHabitsScreen: ListHabitsScreen
    abstract val listHabitsSelectionMenu: ListHabitsSelectionMenu
    abstract val themeSwitcher: ThemeSwitcher

    @ActivityScope
    @Provides
    open fun themeSwitcher(
        @ActivityContext context: Context,
        prefs: Preferences
    ): ThemeSwitcher = AndroidThemeSwitcher(context, prefs)

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
