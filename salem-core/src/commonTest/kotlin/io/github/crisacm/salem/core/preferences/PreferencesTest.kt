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
package io.github.crisacm.salem.core.preferences

import io.github.crisacm.salem.core.BaseUnitTest
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.getFirstWeekdayNumberAccordingToLocale
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PreferencesTest : BaseUnitTest() {
    private lateinit var prefs: Preferences

    private var listener: Preferences.Listener = object : Preferences.Listener {}
    private lateinit var storage: MemoryStorage

    @BeforeTest
    override fun setUp() {
        super.setUp()
        storage = MemoryStorage()
        prefs = Preferences(storage)
        prefs.addListener(listener)
    }

    @Test
    fun testClear() {
        prefs.setDefaultHabitColor(99)
        prefs.clear()
        assertEquals(0, prefs.getDefaultHabitColor(0))
    }

    @Test
    fun testHabitColor() {
        assertEquals(999, prefs.getDefaultHabitColor(999))
        prefs.setDefaultHabitColor(10)
        assertEquals(10, prefs.getDefaultHabitColor(999))
    }

    @Test
    fun testDefaultOrder() {
        assertEquals(HabitList.Order.BY_POSITION, prefs.defaultPrimaryOrder)
        prefs.defaultPrimaryOrder = HabitList.Order.BY_SCORE_DESC
        assertEquals(HabitList.Order.BY_SCORE_DESC, prefs.defaultPrimaryOrder)
        storage.putString("pref_default_order", "BOGUS")
        assertEquals(HabitList.Order.BY_POSITION, prefs.defaultPrimaryOrder)
        assertEquals(
            "BY_POSITION",
            storage.getString("pref_default_order", "")
        )
    }

    @Test
    fun testScoreCardSpinnerPosition() {
        assertEquals(1, prefs.scoreCardSpinnerPosition)
        prefs.scoreCardSpinnerPosition = 4
        assertEquals(4, prefs.scoreCardSpinnerPosition)
        storage.putInt("pref_score_view_interval", 9000)
        assertEquals(4, prefs.scoreCardSpinnerPosition)
    }

    @Test
    fun testLastHint() {
        assertEquals(-1, prefs.lastHintNumber)
        assertNull(prefs.lastHintDate)
        val date = LocalDate(2015, 3, 15)
        prefs.updateLastHint(34, date)
        assertEquals(34, prefs.lastHintNumber)
        assertEquals(date, prefs.lastHintDate)
    }

    @Test
    fun testTheme() {
        assertEquals(ThemeSwitcher.THEME_AUTOMATIC, prefs.theme)
        prefs.theme = ThemeSwitcher.THEME_DARK
        assertEquals(ThemeSwitcher.THEME_DARK, prefs.theme)
        assertFalse(prefs.isPureBlackEnabled)
        prefs.isPureBlackEnabled = true
        assertTrue(prefs.isPureBlackEnabled)
    }

    @Test
    fun testNotifications() {
        assertFalse(prefs.shouldMakeNotificationsSticky())
        prefs.setNotificationsSticky(true)
        assertTrue(prefs.shouldMakeNotificationsSticky())
    }

    @Test
    fun testAppVersionAndLaunch() {
        assertEquals(0, prefs.lastAppVersion)
        prefs.lastAppVersion = 23
        assertEquals(23, prefs.lastAppVersion)
        assertTrue(prefs.isFirstRun)
        prefs.isFirstRun = false
        assertFalse(prefs.isFirstRun)
        assertEquals(0, prefs.launchCount)
        prefs.incrementLaunchCount()
        assertEquals(1, prefs.launchCount)
    }

    @Test
    fun testCheckmarks() {
        assertFalse(prefs.isCheckmarkSequenceReversed)
        prefs.isCheckmarkSequenceReversed = true
        assertTrue(prefs.isCheckmarkSequenceReversed)
        assertFalse(prefs.isShortToggleEnabled)
        prefs.isShortToggleEnabled = true
        assertTrue(prefs.isShortToggleEnabled)
    }

    @Test
    fun testDeveloper() {
        assertFalse(prefs.isDeveloper)
        prefs.isDeveloper = true
        assertTrue(prefs.isDeveloper)
    }

    @Test
    fun testFiltering() {
        assertFalse(prefs.showArchived)
        assertTrue(prefs.showCompleted)
        prefs.showArchived = true
        prefs.showCompleted = false
        assertTrue(prefs.showArchived)
        assertFalse(prefs.showCompleted)
    }

    @Test
    fun testMidnightDelay() {
        assertFalse(prefs.isMidnightDelayEnabled)
        prefs.isMidnightDelayEnabled = true
        assertTrue(prefs.isMidnightDelayEnabled)
    }

    private fun localeWeekday() =
        DayOfWeek.entries.first { it.daysSinceSunday + 1 == getFirstWeekdayNumberAccordingToLocale() }

    @Test
    fun testFirstWeekdayFollowsTheLocaleUntilItIsSet() {
        assertEquals(localeWeekday(), prefs.firstWeekday)
        // A chosen day replaces the locale, and removing it brings the locale back
        val other = DayOfWeek.entries.first { it != localeWeekday() }
        prefs.firstWeekday = other
        assertEquals(other, prefs.firstWeekday)
        storage.remove("pref_first_weekday")
        assertEquals(localeWeekday(), prefs.firstWeekday)
    }

    @Test
    fun testFirstWeekdayFallsBackToTheLocaleOnAStoredValueThatMakesNoSense() {
        for (stored in listOf("", "0", "8", "-1", "-5", "abc", "2.5")) {
            storage.putString("pref_first_weekday", stored)
            assertEquals(localeWeekday(), prefs.firstWeekday, "stored '$stored'")
        }
    }

    @Test
    fun testFirstWeekdayIsStoredSundayBased() {
        for (day in DayOfWeek.entries) {
            prefs.firstWeekday = day
            assertEquals(day, prefs.firstWeekday)
            assertEquals((day.daysSinceSunday + 1).toString(), storage.getString("pref_first_weekday", ""))
        }
    }

    @Test
    fun testFirstWeekdayReadsWhatTheOldSettingsWrote() {
        storage.putString("pref_first_weekday", "2")
        assertEquals(DayOfWeek.MONDAY, prefs.firstWeekday)
        storage.putString("pref_first_weekday", "7")
        assertEquals(DayOfWeek.SATURDAY, prefs.firstWeekday)
    }
}
