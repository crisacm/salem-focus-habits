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
import io.github.crisacm.salem.platform.time.DateUtils
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.fixedOffsetZone
import io.github.crisacm.salem.platform.time.setToday
import io.github.crisacm.salem.platform.time.utcTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TodayReminderChangeTest : BaseUnitTest() {
    private val monday = LocalDate(2015, 1, 26)
    private val monday830 = utcTime(2015, 1, 26, 8, 30)
    private val before = utcTime(2015, 1, 26, 6, 0)
    private val after = utcTime(2015, 1, 26, 9, 0)
    private val id = AppleSystemScheduler.identifierOf(7, monday)

    @BeforeTest
    override fun setUp() {
        super.setUp()
        DateUtils.setFixedTimeZone(fixedOffsetZone(0))
        setToday(monday)
    }

    @AfterTest
    fun tearDown() {
        DateUtils.setFixedTimeZone(null)
    }

    private fun habit(days: WeekdayList = WeekdayList.EVERY_DAY, done: Boolean = false, question: String = "Read?"): Habit {
        val habit = fixtures.createEmptyHabit(name = "Read")
        habit.id = 7
        habit.question = question
        habit.reminder = Reminder(8, 30, days)
        if (done) habit.originalEntries.add(Entry(monday, Entry.YES_MANUAL))
        habit.recompute()
        return habit
    }

    @Test
    fun testADoneHabitLosesTodaysRequest() {
        assertEquals(TodayReminderChange.Remove(id), todayReminderChange(habit(done = true), before, "Body"))
    }

    @Test
    fun testADoneHabitLosesTodaysRequestEvenAfterItsTime() {
        assertEquals(TodayReminderChange.Remove(id), todayReminderChange(habit(done = true), after, "Body"))
    }

    @Test
    fun testAHabitThatIsNotDoneGetsTodaysRequestBackBeforeItsTime() {
        val expected = ReminderRequest(id, "Read", "Read?", monday830, 7, utcTime(2015, 1, 26), true)
        assertEquals(TodayReminderChange.Add(expected), todayReminderChange(habit(), before, "Body"))
    }

    @Test
    fun testAHabitWithoutAQuestionUsesTheDefaultBody() {
        val change = todayReminderChange(habit(question = ""), before, "Body") as TodayReminderChange.Add
        assertEquals("Body", change.request.body)
    }

    @Test
    fun testAHabitThatIsNotDoneIsLeftAloneAfterItsTime() {
        assertEquals(TodayReminderChange.Nothing, todayReminderChange(habit(), after, "Body"))
    }

    @Test
    fun testADayWithoutReminderDoesNothing() {
        // Only Sunday is enabled, and today is Monday
        val sundayOnly = WeekdayList(1 shl 1)
        assertEquals(TodayReminderChange.Nothing, todayReminderChange(habit(days = sundayOnly, done = true), before, "Body"))
        assertEquals(TodayReminderChange.Nothing, todayReminderChange(habit(days = sundayOnly), before, "Body"))
    }

    @Test
    fun testAHabitWithoutReminderOrIdDoesNothing() {
        val noReminder = habit().also { it.reminder = null }
        assertEquals(TodayReminderChange.Nothing, todayReminderChange(noReminder, before, "Body"))
        val noId = habit().also { it.id = null }
        assertEquals(TodayReminderChange.Nothing, todayReminderChange(noId, before, "Body"))
    }

    @Test
    fun testAHabitWithAMaximumIsNeverRemoved() {
        val limited = fixtures.createEmptyNumericalHabit(NumericalHabitType.AT_MOST)
        limited.id = 7
        limited.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        limited.originalEntries.add(Entry(monday, 1000))
        limited.recompute()
        assertEquals(true, todayReminderChange(limited, before, "Body") is TodayReminderChange.Add)
    }

    private class RecordingCenter : PendingNotificationCenter {
        val calls = mutableListOf<String>()

        override fun add(request: ReminderRequest) {
            calls += "add ${request.identifier}"
        }

        override fun remove(identifier: String) {
            calls += "remove $identifier"
        }

        override fun removeAll() {
            calls += "removeAll"
        }
    }

    @Test
    fun testUpdatingAppliesTheChangeToTheCenter() {
        val center = RecordingCenter()
        updateTodayReminder(center, habit(done = true), before, "Body")
        updateTodayReminder(center, habit(), before, "Body")
        updateTodayReminder(center, habit(), after, "Body")
        assertEquals(listOf("remove $id", "add $id"), center.calls)
    }
}
