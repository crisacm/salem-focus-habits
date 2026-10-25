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

package io.github.crisacm.salem.ui.dialogs

/** Copy of [days] with [index] set to [checked]. */
fun toggleDay(days: BooleanArray, index: Int, checked: Boolean) =
    days.copyOf().also { it[index] = checked }

/**
 * A colour of the palette: [index] is its position in the palette and [number] the one used in its
 * content description ("Color N"), which follows the reading order of the screen.
 */
class Swatch(val index: Int, val number: Int)

/**
 * Rows of swatches in the serpentine layout of the original picker: odd rows are shown backwards,
 * so consecutive colours of the palette stay next to each other from one row to the next.
 */
fun paletteRows(count: Int, columns: Int): List<List<Swatch>> =
    (0 until count).toList().chunked(columns).mapIndexed { row, indexes ->
        val shown = if (row % 2 == 0) indexes else indexes.reversed()
        shown.mapIndexed { column, index -> Swatch(index, row * columns + column + 1) }
    }
