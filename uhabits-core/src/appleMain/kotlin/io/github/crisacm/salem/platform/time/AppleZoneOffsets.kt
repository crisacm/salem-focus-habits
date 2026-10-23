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

package io.github.crisacm.salem.platform.time

import platform.Foundation.NSDate
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.timeZoneWithName

private class AppleZoneOffsets(val zone: NSTimeZone) : ZoneOffsets {
    override fun offsetAt(utcMillis: Long): Long {
        val date = NSDate.dateWithTimeIntervalSince1970(utcMillis / 1000.0)
        return zone.secondsFromGMTForDate(date) * 1000L
    }
}

actual fun systemZoneOffsets(): ZoneOffsets = AppleZoneOffsets(NSTimeZone.localTimeZone)

actual fun zoneOffsetsOf(id: String): ZoneOffsets? =
    NSTimeZone.timeZoneWithName(id)?.let { AppleZoneOffsets(it) }

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
