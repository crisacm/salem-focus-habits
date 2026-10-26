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

import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.ui.widgets.WidgetBehavior
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.detail.DetailNavigation

/**
 * What the user can do from a reminder notification: open the habit, or answer Yes or No for the day the notification
 * was about. A habit that was deleted or archived since the notification was scheduled is ignored. Call these from
 * the main thread, like every other action of the screens.
 */
class ReminderActions(
    private val habitList: HabitList,
    private val widgetBehavior: WidgetBehavior,
    private val navigation: DetailNavigation
) {
    fun open(habitId: Long) {
        activeHabit(habitId)?.let { navigation.open(it) }
    }

    fun check(habitId: Long, date: LocalDate) {
        activeHabit(habitId)?.let { widgetBehavior.onAddRepetition(it, date) }
    }

    fun uncheck(habitId: Long, date: LocalDate) {
        activeHabit(habitId)?.let { widgetBehavior.onRemoveRepetition(it, date) }
    }

    private fun activeHabit(habitId: Long) = habitList.getById(habitId)?.takeUnless { it.isArchived }
}
