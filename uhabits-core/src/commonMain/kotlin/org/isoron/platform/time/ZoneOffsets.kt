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

/**
 * Offset of a time zone from UTC, which depends on the instant because of daylight saving time.
 */
interface ZoneOffsets {
    /**
     * Milliseconds to add to [utcMillis] to get the wall clock time in this zone.
     */
    fun offsetAt(utcMillis: Long): Long
}

/**
 * A zone that is always [offsetMillis] ahead of UTC.
 */
fun fixedOffsetZone(offsetMillis: Long): ZoneOffsets =
    object : ZoneOffsets {
        override fun offsetAt(utcMillis: Long): Long = offsetMillis
    }

/**
 * The time zone currently configured on the device.
 */
expect fun systemZoneOffsets(): ZoneOffsets

/**
 * The zone with the given IANA identifier (for example "Australia/Sydney" or "GMT"), or null if
 * the platform does not know it.
 */
expect fun zoneOffsetsOf(id: String): ZoneOffsets?

/**
 * Milliseconds since the Unix epoch, according to the system clock.
 */
expect fun currentTimeMillis(): Long
