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

/**
 * A local notification that should fire once at [fireTimeMillis] (milliseconds since the Unix epoch).
 * [timestamp] is the start of the day it is about, in the same unit as the Android intent extra.
 * [hasActions] adds the Yes/No buttons, which only make sense for yes/no habits.
 */
data class ReminderRequest(
    val identifier: String,
    val title: String,
    val body: String,
    val fireTimeMillis: Long,
    val habitId: Long,
    val timestamp: Long,
    val hasActions: Boolean = false
)

/**
 * The part of UNUserNotificationCenter that reminders need. It is an interface because the real
 * center cannot be created outside an app bundle, which includes the unit test host.
 */
interface PendingNotificationCenter {
    /**
     * Adds the request, replacing a pending one with the same identifier.
     */
    fun add(request: ReminderRequest)

    fun remove(identifier: String)

    fun removeAll()
}
