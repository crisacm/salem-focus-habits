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

package io.github.crisacm.salem.ui.settings

import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.platform.time.DayOfWeek
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsStateTest {
    private val storage = MemoryStorage()
    private val preferences = Preferences(storage)
    private var changes = 0
    private val state = SettingsState(preferences) { changes++ }

    @Test
    fun startsWithTheStoredValues() {
        preferences.isPureBlackEnabled = true
        preferences.firstWeekday = DayOfWeek.TUESDAY
        val fresh = SettingsState(preferences)
        assertTrue(fresh.isOn(SettingsSwitch.PURE_BLACK))
        assertFalse(fresh.isOn(SettingsSwitch.SKIP))
        assertEquals(DayOfWeek.TUESDAY, fresh.firstWeekday)
    }

    @Test
    fun everySwitchWritesThroughItsSetterOfPreferences() {
        val reads = mapOf<SettingsSwitch, () -> Boolean>(
            SettingsSwitch.SHORT_TOGGLE to { preferences.isShortToggleEnabled },
            SettingsSwitch.MIDNIGHT_DELAY to { preferences.isMidnightDelayEnabled },
            SettingsSwitch.SKIP to { preferences.isSkipEnabled },
            SettingsSwitch.UNKNOWN to { preferences.areQuestionMarksEnabled },
            SettingsSwitch.REVERSE_DAYS to { preferences.isCheckmarkSequenceReversed },
            SettingsSwitch.PURE_BLACK to { preferences.isPureBlackEnabled },
            SettingsSwitch.DISABLE_ANIMATION to { preferences.isConfettiAnimationDisabled },
            SettingsSwitch.STICKY_NOTIFICATIONS to { preferences.shouldMakeNotificationsSticky() },
            SettingsSwitch.DEVELOPER to { preferences.isDeveloper }
        )
        assertEquals(SettingsSwitch.entries.toSet(), reads.keys)
        for ((switch, read) in reads) {
            assertFalse(read(), "$switch starts off")
            state.set(switch, true)
            assertTrue(read(), "$switch is written")
            assertTrue(state.isOn(switch), "$switch is shown")
            state.set(switch, false)
            assertFalse(read(), "$switch is cleared")
        }
    }

    @Test
    fun tellsThatSomethingChangedAfterEachWrite() {
        state.set(SettingsSwitch.SKIP, true)
        state.selectFirstWeekday(DayOfWeek.FRIDAY)
        assertEquals(2, changes)
    }

    @Test
    fun theListenersOfPreferencesHearTheChanges() {
        var reversed = 0
        preferences.addListener(object : Preferences.Listener {
            override fun onCheckmarkSequenceChanged() {
                reversed++
            }
        })
        state.set(SettingsSwitch.REVERSE_DAYS, true)
        assertEquals(1, reversed)
    }

    @Test
    fun firstWeekdayIsStoredSundayBased() {
        state.selectFirstWeekday(DayOfWeek.MONDAY)
        assertEquals(DayOfWeek.MONDAY, state.firstWeekday)
        assertEquals("2", storage.getString("pref_first_weekday", ""))
    }
}
