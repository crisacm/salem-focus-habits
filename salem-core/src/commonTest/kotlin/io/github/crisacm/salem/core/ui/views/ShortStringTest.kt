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

package io.github.crisacm.salem.core.ui.views

import kotlin.test.Test
import kotlin.test.assertEquals

class ShortStringTest {
    @Test
    fun smallNumbersKeepTheirDecimalsWithoutTrailingZeros() {
        assertEquals("0", 0.0.toShortString())
        assertEquals("5", 5.0.toShortString())
        assertEquals("0.5", 0.5.toShortString())
        assertEquals("1.25", 1.25.toShortString())
        assertEquals("12.5", 12.5.toShortString())
        assertEquals("99.9", 99.9.toShortString())
    }

    @Test
    fun valuesCloseToAWholeNumberAreShownWhole() {
        assertEquals("5", 5.004.toShortString())
        assertEquals("3", 2.996.toShortString())
        assertEquals("12", 12.04.toShortString())
        assertEquals("13", 12.97.toShortString())
        assertEquals("100", 99.96.toShortString())
        assertEquals("10", 9.996.toShortString())
    }

    @Test
    fun exactHalvesRoundUp() {
        assertEquals("0.13", 0.125.toShortString())
        assertEquals("12.3", 12.25.toShortString())
    }

    @Test
    fun bigNumbersUseASuffix() {
        assertEquals("150", 150.0.toShortString())
        assertEquals("4.3k", 4300.0.toShortString())
        assertEquals("43.0k", 43000.0.toShortString())
        assertEquals("250k", 250000.0.toShortString())
        assertEquals("2.5G", 2.5e9.toShortString())
    }
}
