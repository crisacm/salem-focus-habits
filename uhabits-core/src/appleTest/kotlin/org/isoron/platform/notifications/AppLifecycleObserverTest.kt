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

import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import kotlin.test.Test
import kotlin.test.assertEquals

class AppLifecycleObserverTest {
    private val center = NSNotificationCenter()
    private val events = mutableListOf<String>()

    private fun observer() = AppLifecycleObserver({ events.add("foreground") }, { events.add("background") }, center)

    @Test
    fun testNotificationNamesMatchUIKit() {
        assertEquals(UIApplicationDidBecomeActiveNotification, DID_BECOME_ACTIVE)
        assertEquals(UIApplicationDidEnterBackgroundNotification, DID_ENTER_BACKGROUND)
    }

    @Test
    fun testReportsForegroundAndBackground() {
        val observer = observer()
        center.postNotificationName(DID_BECOME_ACTIVE, null)
        center.postNotificationName(DID_ENTER_BACKGROUND, null)
        center.postNotificationName(DID_BECOME_ACTIVE, null)
        assertEquals(listOf("foreground", "background", "foreground"), events)
        observer.stop()
    }

    @Test
    fun testStopEndsTheNotifications() {
        val observer = observer()
        observer.stop()
        center.postNotificationName(DID_BECOME_ACTIVE, null)
        center.postNotificationName(DID_ENTER_BACKGROUND, null)
        assertEquals(emptyList(), events)
        observer.stop()
    }

    @Test
    fun testOtherNotificationsAreIgnored() {
        val observer = observer()
        center.postNotificationName("SomethingElse", null)
        assertEquals(emptyList(), events)
        observer.stop()
    }
}
