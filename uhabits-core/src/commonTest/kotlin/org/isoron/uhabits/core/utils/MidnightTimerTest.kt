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
    @AfterTest
    fun tearDown() {
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
        val timer = MidnightTimer(StandardLogging(), mock<Preferences>(), StandardTestDispatcher(testScheduler))
        var calls = 0
        timer.addListener { calls++ }
        timer.onResume(delayOffsetInMillis = 1)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(0, calls)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(DateUtils.DAY_LENGTH)
        runCurrent()
        assertEquals(2, calls)
        timer.onPause()
    }

    @Test
    fun testPauseStopsTheNotifications() = runTest {
        fixClockBeforeMidnight()
        val timer = MidnightTimer(StandardLogging(), mock<Preferences>(), StandardTestDispatcher(testScheduler))
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
        val timer = MidnightTimer(StandardLogging(), mock<Preferences>(), StandardTestDispatcher(testScheduler))
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
        val timer = MidnightTimer(StandardLogging(), mock<Preferences>(), StandardTestDispatcher(testScheduler))
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
