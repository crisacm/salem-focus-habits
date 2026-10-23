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

package org.isoron.uhabits.core.utils

import dev.mokkery.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.isoron.platform.time.DateUtils
import org.isoron.platform.time.utcTime
import org.isoron.platform.time.zoneOffsetsOf
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.io.StandardLogging
import org.isoron.uhabits.core.preferences.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MidnightTimerTest : BaseUnitTest() {
    private var timers = mutableListOf<MidnightTimer>()

    private fun newTimer(scheduler: TestCoroutineScheduler) =
        MidnightTimer(StandardLogging(), mock<Preferences>(), StandardTestDispatcher(scheduler)).also { timers.add(it) }

    @AfterTest
    fun tearDown() {
        // A failed assertion must not leave a loop running in the test scheduler
        timers.forEach { it.onPause() }
        DateUtils.setFixedLocalTime(null)
        DateUtils.setFixedTimeZone(null)
    }

    // One millisecond before midnight in GMT
    private fun fixClockBeforeMidnight() {
        DateUtils.setFixedTimeZone(zoneOffsetsOf("GMT"))
        DateUtils.setFixedLocalTime(utcTime(2017, 1, 1, 23, 59) + DateUtils.MINUTE_LENGTH - 1)
    }

    @Test
    fun testNotifiesListenersAtMidnightAndThenEveryDay() = runTest {
        fixClockBeforeMidnight()
        val timer = newTimer(testScheduler)
        var calls = 0
        timer.addListener {
            calls++
            // The wall clock one millisecond after the midnight that just happened
            DateUtils.setFixedLocalTime(utcTime(2017, 1, 1 + calls) + 1)
        }
        timer.onResume(delayOffsetInMillis = 1)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(0, calls)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(DateUtils.DAY_LENGTH - 1)
        runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, calls)
        timer.onPause()
    }

    @Test
    fun testAListenerThatThrowsDoesNotStopTheOthersOrTheNextDays() = runTest {
        fixClockBeforeMidnight()
        val timer = newTimer(testScheduler)
        var calls = 0
        timer.addListener { throw IllegalStateException("broken listener") }
        timer.addListener {
            calls++
            DateUtils.setFixedLocalTime(utcTime(2017, 1, 1 + calls) + 1)
        }
        timer.onResume(delayOffsetInMillis = 1)

        advanceTimeBy(2)
        runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(DateUtils.DAY_LENGTH)
        runCurrent()
        assertEquals(2, calls)
        timer.onPause()
    }

    @Test
    fun testTheNextNotificationFollowsTheLengthOfTheDay() = runTest {
        // 2017-10-01 has 23 hours in Sydney: daylight saving starts at 02:00.
        DateUtils.setFixedTimeZone(zoneOffsetsOf("Australia/Sydney"))
        DateUtils.setFixedLocalTime(utcTime(2017, 9, 30, 23, 59) + DateUtils.MINUTE_LENGTH - 1)
        val timer = newTimer(testScheduler)
        var calls = 0
        timer.addListener {
            calls++
            DateUtils.setFixedLocalTime(utcTime(2017, 10, 1) + 1)
        }
        timer.onResume(delayOffsetInMillis = 1)

        advanceTimeBy(2)
        runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(23 * DateUtils.HOUR_LENGTH - 1)
        runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, calls)
        timer.onPause()
    }

    @Test
    fun testPauseStopsTheNotifications() = runTest {
        fixClockBeforeMidnight()
        val timer = newTimer(testScheduler)
        var calls = 0
        timer.addListener { calls++ }
        timer.onResume(delayOffsetInMillis = 1)
        timer.onPause()
        advanceTimeBy(3 * DateUtils.DAY_LENGTH)
        runCurrent()
        assertEquals(0, calls)
    }

    @Test
    fun testResumeTwiceKeepsASingleSchedule() = runTest {
        fixClockBeforeMidnight()
        val timer = newTimer(testScheduler)
        var calls = 0
        timer.addListener { calls++ }
        timer.onResume(delayOffsetInMillis = 1)
        timer.onResume(delayOffsetInMillis = 1)
        advanceTimeBy(2)
        runCurrent()
        assertEquals(1, calls)
        timer.onPause()
    }

    @Test
    fun testRemovedListenersAreNotNotified() = runTest {
        fixClockBeforeMidnight()
        val timer = newTimer(testScheduler)
        var calls = 0
        val listener = MidnightTimer.MidnightListener { calls++ }
        timer.addListener(listener)
        timer.removeListener(listener)
        timer.onResume(delayOffsetInMillis = 1)
        advanceTimeBy(2)
        runCurrent()
        assertEquals(0, calls)
        timer.onPause()
    }

    @Test
    fun testPauseWithoutResumeIsHarmless() {
        MidnightTimer(StandardLogging(), mock<Preferences>()).onPause()
    }
}
