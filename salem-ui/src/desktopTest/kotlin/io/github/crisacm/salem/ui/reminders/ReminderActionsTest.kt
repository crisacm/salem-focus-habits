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

import dev.mokkery.mock
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.NotificationTray
import io.github.crisacm.salem.core.ui.widgets.WidgetBehavior
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.ui.BaseUiTest
import io.github.crisacm.salem.ui.detail.DetailNavigation
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ReminderActionsTest : BaseUiTest() {
    private lateinit var navigation: DetailNavigation
    private lateinit var actions: ReminderActions
    private lateinit var habit: Habit

    @Before
    override fun setUp() {
        super.setUp()
        navigation = DetailNavigation()
        val behavior = WidgetBehavior(habitList, commandRunner, mock<NotificationTray>(), Preferences(MemoryStorage()))
        actions = ReminderActions(habitList, behavior, navigation)
        // The fixtures do not add the habit to the list, which also gives it an id
        habit = fixtures.createEmptyHabit().also { habitList.add(it) }
    }

    private fun valueOn(habit: Habit, daysAgo: Int = 0) =
        habit.originalEntries.get(getToday().minus(daysAgo)).value

    @Test
    fun openShowsTheDetailOfTheHabit() {
        actions.open(habit.id!!)
        assertSame(habit, navigation.habit)
    }

    @Test
    fun checkAddsARepetitionOnTheDayOfTheNotification() {
        actions.check(habit.id!!, getToday().minus(1))
        assertEquals(Entry.YES_MANUAL, valueOn(habit, daysAgo = 1))
        assertEquals(Entry.UNKNOWN, valueOn(habit, daysAgo = 0))
    }

    @Test
    fun uncheckMarksTheDayAsNotDone() {
        actions.check(habit.id!!, getToday())
        actions.uncheck(habit.id!!, getToday())
        assertEquals(Entry.NO, valueOn(habit))
    }

    @Test
    fun aHabitThatNoLongerExistsIsIgnored() {
        val missing = habit.id!! + 1000
        actions.open(missing)
        actions.check(missing, getToday())
        actions.uncheck(missing, getToday())
        assertNull(navigation.habit)
    }

    @Test
    fun anArchivedHabitIsIgnored() {
        habit.isArchived = true
        actions.open(habit.id!!)
        actions.check(habit.id!!, getToday())
        assertNull(navigation.habit)
        assertEquals(Entry.UNKNOWN, valueOn(habit))
    }
}
