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

package org.isoron.platform.time

import java.util.TimeZone

private class JavaZoneOffsets(val zone: TimeZone) : ZoneOffsets {
    override fun offsetAt(utcMillis: Long): Long = zone.getOffset(utcMillis).toLong()
}

actual fun systemZoneOffsets(): ZoneOffsets = JavaZoneOffsets(TimeZone.getDefault())

actual fun zoneOffsetsOf(id: String): ZoneOffsets? =
    if (id in TimeZone.getAvailableIDs()) JavaZoneOffsets(TimeZone.getTimeZone(id)) else null

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

/**
 * Fixes the zone used by [DateUtils] to a [java.util.TimeZone], or restores the system zone with null.
 */
fun DateUtils.setFixedTimeZone(value: TimeZone?) {
    setFixedTimeZone(value?.let { JavaZoneOffsets(it) })
}
