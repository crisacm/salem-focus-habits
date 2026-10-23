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

package io.github.crisacm.salem.platform.notifications

import dev.mokkery.mock
import io.github.crisacm.salem.core.BaseUnitTest
import io.github.crisacm.salem.core.commands.ArchiveHabitsCommand
import io.github.crisacm.salem.core.commands.ChangeHabitColorCommand
import io.github.crisacm.salem.core.commands.CreateRepetitionCommand
import io.github.crisacm.salem.core.commands.DeleteHabitsCommand
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.Reminder
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.core.preferences.WidgetPreferences
import io.github.crisacm.salem.core.reminders.ReminderScheduler
import io.github.crisacm.salem.platform.time.DateUtils
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.fixedOffsetZone
import io.github.crisacm.salem.platform.time.setToday
import io.github.crisacm.salem.platform.time.utcTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class SyncCenter : PendingNotificationCenter {
    val requests = LinkedHashMap<String, ReminderRequest>()
    var cleared = 0

    override fun add(request: ReminderRequest) {
        requests[request.identifier] = request
    }

    override fun remove(identifier: String) {
        requests.remove(identifier)
    }

    override fun removeAll() {
        cleared++
        requests.clear()
    }
}

class AppleReminderSyncTest : BaseUnitTest() {
    private val center = SyncCenter()
    private lateinit var sync: AppleReminderSync
    private lateinit var habit: Habit

    @BeforeTest
    override fun setUp() {
        super.setUp()
        DateUtils.setFixedTimeZone(fixedOffsetZone(0))
        DateUtils.setFixedLocalTime(utcTime(2015, 1, 26, 6, 0))
        setToday(LocalDate(2015, 1, 26))
        val system = AppleSystemScheduler(center, "Time to check in", { utcTime(2015, 1, 26, 6, 0) }, {})
        val scheduler = ReminderScheduler(commandRunner, habitList, system, mock<WidgetPreferences>())
        sync = AppleReminderSync(commandRunner, scheduler, system)
        habit = fixtures.createEmptyHabit()
        habit.id = 7
        habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        habitList.add(habit)
        scheduler.scheduleAll()
    }

    @AfterTest
    fun tearDown() {
        DateUtils.setFixedTimeZone(null)
        DateUtils.setFixedLocalTime(null)
        sync.stopListening()
    }

    @Test
    fun testRemovingTheReminderRemovesItsPendingNotifications() {
        assertEquals(7, center.requests.size)
        habit.reminder = null
        sync.onCommandFinished(ArchiveHabitsCommand(habitList, emptyList()))
        assertEquals(0, center.requests.size)
    }

    @Test
    fun testDeletingTheHabitRemovesItsPendingNotifications() {
        habitList.remove(habit)
        sync.onCommandFinished(DeleteHabitsCommand(habitList, listOf(habit)))
        assertEquals(0, center.requests.size)
    }

    @Test
    fun testArchivingTheHabitRemovesItsPendingNotifications() {
        habit.isArchived = true
        sync.onCommandFinished(ArchiveHabitsCommand(habitList, listOf(habit)))
        assertEquals(0, center.requests.size)
    }

    @Test
    fun testOtherHabitsKeepTheirNotifications() {
        val other = fixtures.createEmptyHabit()
        other.id = 8
        other.reminder = Reminder(9, 0, WeekdayList.EVERY_DAY)
        habitList.add(other)
        sync.onCommandFinished(ArchiveHabitsCommand(habitList, emptyList()))
        assertEquals(14, center.requests.size)
        habitList.remove(habit)
        sync.onCommandFinished(DeleteHabitsCommand(habitList, listOf(habit)))
        assertEquals(setOf(8L), center.requests.values.map { it.habitId }.toSet())
    }

    @Test
    fun testChecksAndColorChangesLeaveTheNotificationsAlone() {
        val before = center.cleared
        sync.onCommandFinished(CreateRepetitionCommand(habitList, habit, LocalDate(2015, 1, 26), 2, ""))
        sync.onCommandFinished(ChangeHabitColorCommand(habitList, listOf(habit), habit.color))
        assertEquals(before, center.cleared)
    }

    @Test
    fun testItReschedulesWhenItListensToTheCommandRunner() {
        sync.startListening()
        habit.reminder = null
        commandRunner.run(ArchiveHabitsCommand(habitList, emptyList()))
        assertEquals(0, center.requests.size)
        sync.stopListening()
        habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        commandRunner.run(ArchiveHabitsCommand(habitList, emptyList()))
        assertEquals(0, center.requests.size)
    }
}
