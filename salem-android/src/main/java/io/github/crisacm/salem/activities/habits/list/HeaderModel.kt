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

package io.github.crisacm.salem.activities.habits.list

import io.github.crisacm.salem.platform.time.LocalDate

/** The direction in which a horizontal drag moves the header: reversed order and right-to-left each flip it. */
fun scrollDirection(reversed: Boolean, rtl: Boolean): Int {
    var direction = -1
    if (reversed) direction *= -1
    if (rtl) direction *= -1
    return direction
}

/**
 * The dates of the header columns, listed in reading order (the first one is drawn first). Column
 * `index` is `index + dataOffset` days before [today]; the reversed order lists them the other way round.
 */
fun headerColumns(count: Int, dataOffset: Int, today: LocalDate, reversed: Boolean): List<LocalDate> {
    val dates = List(count) { today.minus(it + dataOffset) }
    return if (reversed) dates.reversed() else dates
}

/**
 * The horizontal scroll of the header. [x] is the dragged distance in pixels; one day is [bucketPx]
 * pixels wide and the offset in days never leaves `0..maxOffset`.
 */
class HeaderScroll(var bucketPx: Float = 1f, maxOffset: Int = 0) {
    var maxOffset = maxOffset
        private set
    var x = 0f
        private set

    val dataOffset: Int
        get() = if (bucketPx <= 0f) 0 else (x / bucketPx).toInt().coerceIn(0, maxOffset)

    val maxX get() = maxOffset * bucketPx

    fun dragBy(delta: Float, direction: Int) {
        moveTo(x + direction * delta)
    }

    fun moveTo(newX: Float) {
        x = newX.coerceIn(0f, maxX)
    }

    fun setOffset(offset: Int) = moveTo(offset * bucketPx)

    fun updateMax(max: Int) {
        maxOffset = max.coerceAtLeast(0)
        moveTo(x)
    }
}
