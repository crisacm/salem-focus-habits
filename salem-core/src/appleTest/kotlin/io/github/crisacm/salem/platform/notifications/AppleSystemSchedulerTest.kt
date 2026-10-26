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

import io.github.crisacm.salem.core.BaseUnitTest
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.models.Reminder
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.core.reminders.ReminderScheduler.SchedulerResult
import io.github.crisacm.salem.platform.time.DateUtils
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.fixedOffsetZone
import io.github.crisacm.salem.platform.time.setToday
import io.github.crisacm.salem.platform.time.utcTime
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
        scheduler = scheduler(limit = 64)
    }

    @AfterTest
    fun tearDown() {
        DateUtils.setFixedTimeZone(null)
    }

    private fun scheduler(limit: Int, horizon: Int = 7) =
        AppleSystemScheduler(center, "Time to check in", { now }, { logs.add(it) }, limit, horizon)

    private fun habit(id: Long, name: String = "Habit $id", question: String = "", days: WeekdayList = WeekdayList.EVERY_DAY): Habit {
        val habit = fixtures.createEmptyHabit()
        habit.id = id
        habit.name = name
        habit.question = question
        habit.reminder = Reminder(8, 30, days)
        return habit
    }

    private fun idOf(habitId: Long, year: Int, month: Int, day: Int) =
        AppleSystemScheduler.identifierOf(habitId, LocalDate(year, month, day))

    private val monday830 = utcTime(2015, 1, 26, 8, 30)
    private val monday0 = utcTime(2015, 1, 26)
    private val dayLength = DateUtils.DAY_LENGTH

    @Test
    fun testSchedulesTheReminderWithItsContent() {
        val result = scheduler.scheduleShowReminder(monday830, habit(7, "Read", "Did you read?"), monday0)
        assertEquals(SchedulerResult.OK, result)
        assertEquals(
            ReminderRequest(idOf(7, 2015, 1, 26), "Read", "Did you read?", monday830, 7, monday0, true),
            center.requests[idOf(7, 2015, 1, 26)]
        )
    }

    @Test
    fun testSchedulesOneRequestPerDayOfTheHorizon() {
        scheduler.scheduleShowReminder(monday830, habit(7), monday0)
        assertEquals(7, center.requests.size)
        for (k in 0L..6L) {
            val request = center.requests.values.toList()[k.toInt()]
            assertEquals(monday830 + k * dayLength, request.fireTimeMillis)
            assertEquals(monday0 + k * dayLength, request.timestamp)
        }
        assertEquals(idOf(7, 2015, 2, 1), center.requests.values.last().identifier)
    }

    @Test
    fun testTheHorizonIsConfigurable() {
        scheduler(limit = 64, horizon = 2).scheduleShowReminder(monday830, habit(7), monday0)
        assertEquals(setOf(idOf(7, 2015, 1, 26), idOf(7, 2015, 1, 27)), center.requests.keys)
    }

    @Test
    fun testUsesTheDefaultBodyWhenThereIsNoQuestion() {
        scheduler.scheduleShowReminder(monday830, habit(7, "Read", "  "), monday0)
        assertEquals("Time to check in", center.requests[idOf(7, 2015, 1, 26)]?.body)
    }

    @Test
    fun testSchedulingAgainReplacesTheRequestsOfTheHabit() {
        val h = habit(7)
        scheduler.scheduleShowReminder(monday830, h, monday0)
        scheduler.scheduleShowReminder(monday830 + dayLength, h, monday0 + dayLength)
        // Today's request is gone and the horizon now starts tomorrow
        assertEquals(7, center.requests.size)
        assertEquals(false, idOf(7, 2015, 1, 26) in center.requests)
        assertEquals(true, idOf(7, 2015, 2, 2) in center.requests)
    }

    @Test
    fun testTimesInThePastAreSkipped() {
        now = utcTime(2015, 1, 26, 9, 0)
        scheduler.scheduleShowReminder(monday830, habit(7), monday0)
        assertEquals(6, center.requests.size)
        assertEquals(false, idOf(7, 2015, 1, 26) in center.requests)
    }

    @Test
    fun testOnlyPastTimesAreIgnored() {
        now = utcTime(2015, 1, 26, 9, 0)
        val result = scheduler(limit = 64, horizon = 1).scheduleShowReminder(monday830, habit(7), monday0)
        assertEquals(SchedulerResult.IGNORED, result)
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
    fun testOnlyTheEnabledDaysAreScheduled() {
        val h = habit(7, days = daysOnly(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        assertEquals(SchedulerResult.OK, scheduler.scheduleShowReminder(monday830, h, monday0))
        assertEquals(setOf(idOf(7, 2015, 1, 28), idOf(7, 2015, 1, 30)), center.requests.keys)
        val request = center.requests[idOf(7, 2015, 1, 28)]
        assertEquals(utcTime(2015, 1, 28, 8, 30), request?.fireTimeMillis)
        assertEquals(utcTime(2015, 1, 28), request?.timestamp)
    }

    @Test
    fun testAHabitWithNoEnabledDayRemovesItsRequests() {
        val h = habit(7)
        scheduler.scheduleShowReminder(monday830, h, monday0)
        h.reminder = Reminder(8, 30, daysOnly())
        assertEquals(SchedulerResult.IGNORED, scheduler.scheduleShowReminder(monday830, h, monday0))
        assertEquals(0, center.requests.size)
    }

    @Test
    fun testASnoozedTimeIsScheduledOnlyOnce() {
        val snoozed = utcTime(2015, 1, 26, 21, 15)
        scheduler.scheduleShowReminder(snoozed, habit(7), monday0)
        assertEquals(setOf(idOf(7, 2015, 1, 26)), center.requests.keys)
        assertEquals(snoozed, center.requests.values.single().fireTimeMillis)
    }

    @Test
    fun testKeepsTheEarliestRemindersAcrossHabitsWhenOverTheLimit() {
        val small = scheduler(limit = 3, horizon = 2)
        small.scheduleShowReminder(monday830, habit(1), monday0)
        assertEquals(2, center.requests.size)
        // Habit 2 at 09:00 today and tomorrow: only the earliest three of the four stay
        val h2 = habit(2).also { it.reminder = Reminder(9, 0, WeekdayList.EVERY_DAY) }
        assertEquals(
            SchedulerResult.OK,
            small.scheduleShowReminder(utcTime(2015, 1, 26, 9, 0), h2, monday0)
        )
        assertEquals(
            setOf(idOf(1, 2015, 1, 26), idOf(2, 2015, 1, 26), idOf(1, 2015, 1, 27)),
            center.requests.keys
        )
        assertEquals(true, logs.any { "reminder-2-" in it })
    }

    @Test
    fun testRemindersThatAlreadyFiredDoNotCountForTheLimit() {
        val small = scheduler(limit = 2, horizon = 2)
        small.scheduleShowReminder(monday830, habit(1), monday0)
        // Habit 1's reminder of today has fired by now; it must not push out the later ones
        now = utcTime(2015, 1, 26, 8, 45)
        val h2 = habit(2).also { it.reminder = Reminder(9, 0, WeekdayList.EVERY_DAY) }
        small.scheduleShowReminder(utcTime(2015, 1, 26, 9, 0), h2, monday0)
        assertEquals(true, idOf(2, 2015, 1, 26) in center.requests)
        assertEquals(true, idOf(1, 2015, 1, 27) in center.requests)
        assertEquals(false, idOf(2, 2015, 1, 27) in center.requests)
    }

    @Test
    fun testTheNewHabitIsIgnoredWhenAllItsRemindersAreTheLatest() {
        val small = scheduler(limit = 2, horizon = 2)
        small.scheduleShowReminder(monday830, habit(1), monday0)
        val late = habit(2).also { it.reminder = Reminder(23, 0, WeekdayList.EVERY_DAY) }
        // Habit 2 fires after both of habit 1's, so it does not fit
        assertEquals(
            SchedulerResult.IGNORED,
            small.scheduleShowReminder(utcTime(2015, 1, 27, 23, 0), late, monday0)
        )
        assertEquals(setOf(idOf(1, 2015, 1, 26), idOf(1, 2015, 1, 27)), center.requests.keys)
    }

    @Test
    fun testClearForgetsEverything() {
        for (id in 1L..3L) scheduler.scheduleShowReminder(monday830, habit(id), monday0)
        scheduler.clear()
        assertEquals(1, center.cleared)
        scheduler.scheduleShowReminder(monday830, habit(4), monday0)
        assertEquals(setOf(4L), center.requests.values.map { it.habitId }.toSet())
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

    private fun completeToday(habit: Habit, value: Int = Entry.YES_MANUAL) {
        setToday(LocalDate(2015, 1, 26))
        habit.originalEntries.add(Entry(LocalDate(2015, 1, 26), value))
        habit.recompute()
    }

    @Test
    fun testACompletedHabitHasNoRequestForToday() {
        val done = habit(7)
        completeToday(done)
        scheduler.scheduleShowReminder(monday830, done, monday0)
        assertEquals(6, center.requests.size)
        assertEquals(false, idOf(7, 2015, 1, 26) in center.requests)
        assertEquals(true, idOf(7, 2015, 1, 27) in center.requests)
    }

    @Test
    fun testAHabitThatIsNotCompletedKeepsTodaysRequest() {
        setToday(LocalDate(2015, 1, 26))
        scheduler.scheduleShowReminder(monday830, habit(7), monday0)
        assertEquals(7, center.requests.size)
        assertEquals(true, idOf(7, 2015, 1, 26) in center.requests)
    }

    @Test
    fun testAHabitWithAMaximumKeepsTodaysRequestEvenWhenCompleted() {
        val limited = fixtures.createEmptyNumericalHabit(NumericalHabitType.AT_MOST)
        limited.id = 8
        limited.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        completeToday(limited, 1000)
        scheduler.scheduleShowReminder(monday830, limited, monday0)
        assertEquals(true, idOf(8, 2015, 1, 26) in center.requests)
    }
}
