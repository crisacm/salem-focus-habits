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
import platform.Foundation.NSOperationQueue

// The names of UIApplication's notifications, as strings so that core does not depend on UIKit.
internal const val DID_BECOME_ACTIVE = "UIApplicationDidBecomeActiveNotification"
internal const val DID_ENTER_BACKGROUND = "UIApplicationDidEnterBackgroundNotification"

/**
 * Reports when the app becomes active and when it goes to the background, by listening to
 * UIApplication's notifications on the main queue. Starts listening when created; call [stop] to end.
 */
class AppLifecycleObserver(
    onForeground: () -> Unit,
    onBackground: () -> Unit,
    private val center: NSNotificationCenter = NSNotificationCenter.defaultCenter
) {
    private val tokens = listOf(
        center.addObserverForName(DID_BECOME_ACTIVE, null, NSOperationQueue.mainQueue) { onForeground() },
        center.addObserverForName(DID_ENTER_BACKGROUND, null, NSOperationQueue.mainQueue) { onBackground() }
    )

    fun stop() {
        for (token in tokens) center.removeObserver(token)
    }
}
