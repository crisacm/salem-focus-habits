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
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.fixedOffsetZone
import org.isoron.platform.time.getFirstWeekday
import org.isoron.platform.time.getToday
import org.isoron.platform.time.resetFirstWeekday
import org.isoron.platform.time.setToday
import org.isoron.platform.time.utcTime
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.io.StandardLogging
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.preferences.MemoryStorage
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.preferences.WidgetPreferences
import org.isoron.uhabits.core.reminders.ReminderScheduler
import org.isoron.uhabits.core.utils.MidnightTimer
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
        assertEquals(setOf("reminder-7"), center.requests.keys)
    }

    @Test
    fun testPendingRemindersAreClearedBeforeSchedulingAgain() {
        refresher.onLaunch()
        center.events.clear()
        refresher.onForeground()
        assertEquals(listOf("removeAll", "add reminder-7"), center.events)
    }

    @Test
    fun testRemindersOfHabitsThatLostTheirsDisappear() {
        refresher.onLaunch()
        habitList.getByPosition(0).reminder = null
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
    fun testBackgroundAfterForegroundDoesNotFail() {
        refresher.onForeground()
        refresher.onBackground()
        refresher.onBackground()
        assertTrue(true)
    }
}
