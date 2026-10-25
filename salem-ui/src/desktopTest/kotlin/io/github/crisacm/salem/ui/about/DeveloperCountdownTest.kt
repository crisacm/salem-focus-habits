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

package io.github.crisacm.salem.ui.about

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeveloperCountdownTest {
    @Test
    fun unlocksOnTheLastTapOnly() {
        val countdown = DeveloperCountdown(taps = 3)
        assertFalse(countdown.onTap())
        assertFalse(countdown.onTap())
        assertTrue(countdown.onTap())
        assertEquals(0, countdown.remaining)
    }

    @Test
    fun staysQuietAfterUnlocking() {
        val countdown = DeveloperCountdown(taps = 1)
        assertTrue(countdown.onTap())
        assertFalse(countdown.onTap())
        assertEquals(0, countdown.remaining)
    }

    @Test
    fun unlocksOnTheFifthTapByDefault() {
        val countdown = DeveloperCountdown()
        assertEquals(5, countdown.remaining)
        repeat(4) { assertFalse(countdown.onTap()) }
        assertTrue(countdown.onTap())
    }
}
