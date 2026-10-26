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

package io.github.crisacm.salem.preferences

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EchoFilterTest {
    private val filter = EchoFilter()

    @Test
    fun aChangeTheAppWroteItselfIsNotNew() {
        filter.written("pref_midnight_delay", true)
        assertFalse(filter.isNew("pref_midnight_delay", true))
    }

    @Test
    fun aChangeMadeBehindTheBackOfTheAppIsNew() {
        filter.written("pref_midnight_delay", true)
        assertTrue(filter.isNew("pref_midnight_delay", false))
    }

    @Test
    fun aValueNobodyWroteBeforeIsNew() {
        assertTrue(filter.isNew("pref_midnight_delay", false))
    }

    @Test
    fun theSameOutsideChangeIsReportedOnce() {
        assertTrue(filter.isNew("pref_unknown_enabled", true))
        assertFalse(filter.isNew("pref_unknown_enabled", true))
    }

    @Test
    fun theKeysDoNotShareTheirValues() {
        filter.written("pref_midnight_delay", true)
        assertTrue(filter.isNew("pref_unknown_enabled", true))
    }

    @Test
    fun forgettingEverythingMakesTheNextChangeNew() {
        filter.written("pref_midnight_delay", true)
        filter.forgetAll()
        assertTrue(filter.isNew("pref_midnight_delay", true))
    }

    @Test
    fun forgettingAKeyMakesItsNextChangeNew() {
        filter.written("pref_midnight_delay", true)
        filter.forget("pref_midnight_delay")
        assertTrue(filter.isNew("pref_midnight_delay", true))
    }
}
