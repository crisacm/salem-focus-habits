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

package io.github.crisacm.salem.activities.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeveloperCountdownTest {
    @Test
    fun unlocksOnFifthTap() {
        val countdown = DeveloperCountdown()
        repeat(4) { assertFalse(countdown.onTap()) }
        assertTrue(countdown.onTap())
    }

    @Test
    fun unlocksOnlyOnce() {
        val countdown = DeveloperCountdown()
        repeat(5) { countdown.onTap() }
        repeat(3) { assertFalse(countdown.onTap()) }
    }

    @Test
    fun exposesRemainingTaps() {
        val countdown = DeveloperCountdown()
        assertEquals(5, countdown.remaining)
        countdown.onTap()
        assertEquals(4, countdown.remaining)
    }
}
