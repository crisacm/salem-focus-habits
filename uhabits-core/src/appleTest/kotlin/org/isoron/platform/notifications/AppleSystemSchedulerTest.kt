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

import org.isoron.platform.time.DateUtils
import org.isoron.platform.time.DayOfWeek
import org.isoron.platform.time.fixedOffsetZone
import org.isoron.platform.time.utcTime
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.reminders.ReminderScheduler.SchedulerResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeCenter : PendingNotificationCenter {
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

class AppleSystemSchedulerTest : BaseUnitTest() {
    private val center = FakeCenter()
    private val logs = mutableListOf<String>()
    private var now = utcTime(2015, 1, 26, 6, 0)
    private lateinit var scheduler: AppleSystemScheduler

    @BeforeTest
    override fun setUp() {
        super.setUp()
        DateUtils.setFixedTimeZone(fixedOffsetZone(0))
        scheduler = AppleSystemScheduler(center, "Time to check in", { now }, { logs.add(it) }, limit = 3)
    }

    @AfterTest
    fun tearDown() {
        DateUtils.setFixedTimeZone(null)
    }

    private fun habit(id: Long, name: String = "Habit $id", question: String = "", days: WeekdayList = WeekdayList.EVERY_DAY): Habit {
        val habit = fixtures.createEmptyHabit()
        habit.id = id
        habit.name = name
        habit.question = question
        habit.reminder = Reminder(8, 30, days)
        return habit
    }

    private val monday830 = utcTime(2015, 1, 26, 8, 30)
    private val monday0 = utcTime(2015, 1, 26)

    @Test
    fun testSchedulesTheReminderWithItsContent() {
        val result = scheduler.scheduleShowReminder(monday830, habit(7, "Read", "Did you read?"), monday0)
        assertEquals(SchedulerResult.OK, result)
        assertEquals(
            ReminderRequest("reminder-7", "Read", "Did you read?", monday830, 7, monday0),
            center.requests["reminder-7"]
        )
    }

    @Test
    fun testUsesTheDefaultBodyWhenThereIsNoQuestion() {
        scheduler.scheduleShowReminder(monday830, habit(7, "Read", "  "), monday0)
        assertEquals("Time to check in", center.requests["reminder-7"]?.body)
    }

    @Test
    fun testSchedulingAgainReplacesTheRequestOfTheHabit() {
        val h = habit(7)
        scheduler.scheduleShowReminder(monday830, h, monday0)
        scheduler.scheduleShowReminder(monday830 + DateUtils.DAY_LENGTH, h, monday0 + DateUtils.DAY_LENGTH)
        assertEquals(1, center.requests.size)
        assertEquals(monday830 + DateUtils.DAY_LENGTH, center.requests["reminder-7"]?.fireTimeMillis)
    }

    @Test
    fun testTimesInThePastAreIgnored() {
        now = monday830
        assertEquals(SchedulerResult.IGNORED, scheduler.scheduleShowReminder(monday830, habit(7), monday0))
        assertEquals(SchedulerResult.IGNORED, scheduler.scheduleShowReminder(monday830 - 1, habit(7), monday0))
        assertEquals(0, center.requests.size)
    }

    @Test
    fun testHabitsWithoutIdOrReminderAreIgnored() {
        val noId = habit(7).also { it.id = null }
        val noReminder = habit(8).also { it.reminder = null }
        assertEquals(SchedulerResult.IGNORED, scheduler.scheduleShowReminder(monday830, noId, monday0))
        assertEquals(SchedulerResult.IGNORED, scheduler.scheduleShowReminder(monday830, noReminder, monday0))
        assertEquals(0, center.requests.size)
    }

    @Test
    fun testMovesTheReminderToTheNextEnabledDay() {
        val h = habit(7, days = daysOnly(DayOfWeek.WEDNESDAY))
        assertEquals(SchedulerResult.OK, scheduler.scheduleShowReminder(monday830, h, monday0))
        val request = center.requests["reminder-7"]
        assertEquals(utcTime(2015, 1, 28, 8, 30), request?.fireTimeMillis)
        assertEquals(utcTime(2015, 1, 28), request?.timestamp)
    }

    @Test
    fun testAHabitWithNoEnabledDayRemovesItsRequest() {
        val h = habit(7)
        scheduler.scheduleShowReminder(monday830, h, monday0)
        h.reminder = Reminder(8, 30, daysOnly())
        assertEquals(SchedulerResult.IGNORED, scheduler.scheduleShowReminder(monday830, h, monday0))
        assertEquals(0, center.requests.size)
    }

    @Test
    fun testKeepsTheEarliestRemindersWhenOverTheLimit() {
        val hour = DateUtils.HOUR_LENGTH
        scheduler.scheduleShowReminder(monday830 + 3 * hour, habit(1), monday0)
        scheduler.scheduleShowReminder(monday830 + 1 * hour, habit(2), monday0)
        scheduler.scheduleShowReminder(monday830 + 2 * hour, habit(3), monday0)
        assertEquals(
            SchedulerResult.OK,
            scheduler.scheduleShowReminder(monday830, habit(4), monday0)
        )
        assertEquals(setOf("reminder-2", "reminder-3", "reminder-4"), center.requests.keys)
        assertEquals(true, logs.any { "reminder-1" in it })
    }

    @Test
    fun testTheNewReminderIsIgnoredWhenItIsTheLatestOverTheLimit() {
        val hour = DateUtils.HOUR_LENGTH
        for (id in 1L..3L) scheduler.scheduleShowReminder(monday830 + id * hour, habit(id), monday0)
        assertEquals(
            SchedulerResult.IGNORED,
            scheduler.scheduleShowReminder(monday830 + 9 * hour, habit(4), monday0)
        )
        assertEquals(setOf("reminder-1", "reminder-2", "reminder-3"), center.requests.keys)
    }

    @Test
    fun testClearForgetsEverything() {
        for (id in 1L..3L) scheduler.scheduleShowReminder(monday830, habit(id), monday0)
        scheduler.clear()
        assertEquals(1, center.cleared)
        scheduler.scheduleShowReminder(monday830, habit(4), monday0)
        assertEquals(setOf("reminder-4"), center.requests.keys)
    }

    @Test
    fun testWidgetUpdatesAreNotSupported() {
        assertEquals(null, scheduler.scheduleWidgetUpdate(monday830))
    }

    private fun daysOnly(vararg enabled: DayOfWeek): WeekdayList {
        val flags = BooleanArray(7)
        for (day in enabled) flags[(day.daysSinceSunday + 1) % 7] = true
        return WeekdayList(flags)
    }
}
