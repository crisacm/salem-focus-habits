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

package org.isoron.platform.notifications

import dev.mokkery.mock
import org.isoron.platform.time.DateUtils
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.fixedOffsetZone
import org.isoron.platform.time.setToday
import org.isoron.platform.time.utcTime
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.preferences.WidgetPreferences
import org.isoron.uhabits.core.reminders.ReminderScheduler
import org.isoron.uhabits.core.reminders.ReminderScheduler.SchedulerResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class RecordingSystem : ReminderScheduler.SystemScheduler {
    val times = mutableListOf<Long>()

    override fun scheduleShowReminder(reminderTime: Long, habit: Habit, timestamp: Long): SchedulerResult {
        times.add(reminderTime)
        return SchedulerResult.OK
    }

    override fun scheduleWidgetUpdate(updateTime: Long): SchedulerResult? = null

    override fun log(componentName: String, msg: String) {}
}

class HabitCompletionRearmerTest : BaseUnitTest() {
    private val system = RecordingSystem()
    private lateinit var rearmer: HabitCompletionRearmer
    private val today = LocalDate(2015, 1, 26)

    @BeforeTest
    override fun setUp() {
        super.setUp()
        DateUtils.setFixedTimeZone(fixedOffsetZone(0))
        setToday(today)
        // Monday 2015-01-26 06:00; the reminder is at 08:30
        DateUtils.setFixedLocalTime(utcTime(2015, 1, 26, 6, 0))
        val scheduler = ReminderScheduler(commandRunner, habitList, system, mock<WidgetPreferences>())
        rearmer = HabitCompletionRearmer(scheduler)
    }

    @AfterTest
    fun tearDown() {
        DateUtils.setFixedTimeZone(null)
        DateUtils.setFixedLocalTime(null)
    }

    private fun habit(numerical: Boolean = false): Habit {
        val habit = if (numerical) fixtures.createEmptyNumericalHabit(NumericalHabitType.AT_MOST) else fixtures.createEmptyHabit()
        habit.id = 7
        habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        habitList.add(habit)
        return habit
    }

    private fun check(habit: Habit, value: Int, date: LocalDate = today) {
        CreateRepetitionCommand(habitList, habit, date, value, "").run()
        rearmer.onCommandFinished(CreateRepetitionCommand(habitList, habit, date, value, ""))
    }

    @Test
    fun testCheckingBeforeTheReminderMovesItToTomorrow() {
        check(habit(), Entry.YES_MANUAL)
        assertEquals(listOf(utcTime(2015, 1, 27, 8, 30)), system.times)
    }

    @Test
    fun testUncheckingRestoresTodaysReminder() {
        val h = habit()
        check(h, Entry.YES_MANUAL)
        check(h, Entry.NO)
        assertEquals(utcTime(2015, 1, 26, 8, 30), system.times.last())
    }

    @Test
    fun testCheckingAfterTheReminderTimeKeepsTomorrow() {
        DateUtils.setFixedLocalTime(utcTime(2015, 1, 26, 9, 0))
        check(habit(), Entry.YES_MANUAL)
        assertEquals(listOf(utcTime(2015, 1, 27, 8, 30)), system.times)
    }

    @Test
    fun testCheckingAnotherDayDoesNotTouchTodaysReminder() {
        check(habit(), Entry.YES_MANUAL, LocalDate(2015, 1, 25))
        assertEquals(emptyList(), system.times)
    }

    @Test
    fun testAtMostHabitsKeepNotifyingWhenCompleted() {
        check(habit(numerical = true), 1000)
        assertEquals(listOf(utcTime(2015, 1, 26, 8, 30)), system.times)
    }

    @Test
    fun testHabitsWithoutReminderAreIgnored() {
        val h = habit()
        h.reminder = null
        check(h, Entry.YES_MANUAL)
        assertEquals(emptyList(), system.times)
    }

    @Test
    fun testOtherCommandsAreIgnored() {
        rearmer.onCommandFinished(org.isoron.uhabits.core.commands.ArchiveHabitsCommand(habitList, listOf(habit())))
        assertEquals(emptyList(), system.times)
    }
}
