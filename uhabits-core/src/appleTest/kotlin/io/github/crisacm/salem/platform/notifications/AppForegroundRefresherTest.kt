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

import io.github.crisacm.salem.platform.time.DateUtils
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.fixedOffsetZone
import io.github.crisacm.salem.platform.time.getFirstWeekday
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.platform.time.resetFirstWeekday
import io.github.crisacm.salem.platform.time.resetToday
import io.github.crisacm.salem.platform.time.setToday
import io.github.crisacm.salem.platform.time.utcTime
import io.github.crisacm.salem.core.BaseUnitTest
import io.github.crisacm.salem.core.io.StandardLogging
import io.github.crisacm.salem.core.models.Reminder
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.preferences.WidgetPreferences
import io.github.crisacm.salem.core.reminders.ReminderScheduler
import io.github.crisacm.salem.core.utils.MidnightTimer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class RecordingCenter : PendingNotificationCenter {
    val events = mutableListOf<String>()
    val requests = LinkedHashMap<String, ReminderRequest>()

    override fun add(request: ReminderRequest) {
        events.add("add ${request.identifier}")
        requests[request.identifier] = request
    }

    override fun remove(identifier: String) {
        events.add("remove $identifier")
        requests.remove(identifier)
    }

    override fun removeAll() {
        events.add("removeAll")
        requests.clear()
    }
}

class AppForegroundRefresherTest : BaseUnitTest() {
    private val center = RecordingCenter()
    private val storage = MemoryStorage()
    private val preferences = Preferences(storage)
    private var calendarDay = LocalDate(2015, 1, 26)
    private lateinit var midnightTimer: MidnightTimer
    private lateinit var refresher: AppForegroundRefresher

    @BeforeTest
    override fun setUp() {
        super.setUp()
        DateUtils.setFixedTimeZone(fixedOffsetZone(0))
        // Monday 2015-01-26 06:00; the reminder of the habit is at 08:30
        DateUtils.setFixedLocalTime(utcTime(2015, 1, 26, 6, 0))
        setToday(LocalDate(2015, 1, 26))
        val system = AppleSystemScheduler(center, "Time to check in", { utcTime(2015, 1, 26, 6, 0) }, {})
        val scheduler = ReminderScheduler(commandRunner, habitList, system, WidgetPreferences(storage))
        midnightTimer = MidnightTimer(StandardLogging(), preferences, todayProvider = { calendarDay })
        refresher = AppForegroundRefresher(preferences, midnightTimer, scheduler, system, { calendarDay })
        val habit = fixtures.createEmptyHabit()
        habit.id = 7
        habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        habitList.add(habit)
    }

    @AfterTest
    fun tearDown() {
        DateUtils.setFixedTimeZone(null)
        DateUtils.setFixedLocalTime(null)
        resetFirstWeekday()
        midnightTimer.onPause()
    }

    @Test
    fun testLaunchSetsTodayAndSchedulesTheReminders() {
        setToday(LocalDate(2000, 1, 1))
        refresher.onLaunch()
        assertEquals(calendarDay, getToday())
        assertEquals(7, center.requests.size)
        assertTrue(center.requests.keys.all { it.startsWith("reminder-7-") })
    }

    @Test
    fun testPendingRemindersAreClearedBeforeSchedulingAgain() {
        refresher.onLaunch()
        center.events.clear()
        refresher.onBackground()
        refresher.onForeground()
        assertEquals("removeAll", center.events.first())
        assertEquals(7, center.events.count { it.startsWith("add reminder-7-") })
        assertEquals(8, center.events.size)
    }

    @Test
    fun testRemindersOfHabitsThatLostTheirsDisappear() {
        refresher.onLaunch()
        habitList.getByPosition(0).reminder = null
        refresher.onBackground()
        refresher.onForeground()
        assertEquals(emptySet(), center.requests.keys)
    }

    @Test
    fun testForegroundAfterMidnightRefreshesTheDayOnce() {
        refresher.onLaunch()
        var calls = 0
        midnightTimer.addListener { calls++ }
        calendarDay = LocalDate(2015, 1, 27)
        refresher.onForeground()
        assertEquals(LocalDate(2015, 1, 27), getToday())
        assertEquals(1, calls)
        refresher.onForeground()
        assertEquals(1, calls)
    }

    @Test
    fun testForegroundOnTheSameDayKeepsListenersQuiet() {
        refresher.onLaunch()
        var calls = 0
        midnightTimer.addListener { calls++ }
        refresher.onForeground()
        assertEquals(0, calls)
    }

    @Test
    fun testForegroundFollowsTheFirstWeekdayPreference() {
        storage.putString("pref_first_weekday", "2")
        refresher.onForeground()
        assertEquals(DayOfWeek.MONDAY, getFirstWeekday())
    }

    @Test
    fun testForegroundBeforeLaunchLaunches() {
        resetToday()
        refresher.onForeground()
        assertEquals(calendarDay, getToday())
        assertEquals(7, center.requests.size)
    }

    @Test
    fun testTheForegroundThatFollowsTheLaunchDoesNotRescheduleAgain() {
        refresher.onLaunch()
        center.events.clear()
        refresher.onForeground()
        assertEquals(emptyList(), center.events)
    }

    @Test
    fun testAForegroundAfterANewDayReschedulesEvenWithoutBackground() {
        refresher.onLaunch()
        center.events.clear()
        calendarDay = LocalDate(2015, 1, 27)
        refresher.onForeground()
        assertEquals("removeAll", center.events.first())
    }

    @Test
    fun testBackgroundAfterForegroundDoesNotFail() {
        refresher.onForeground()
        refresher.onBackground()
        refresher.onBackground()
        assertTrue(true)
    }
}
