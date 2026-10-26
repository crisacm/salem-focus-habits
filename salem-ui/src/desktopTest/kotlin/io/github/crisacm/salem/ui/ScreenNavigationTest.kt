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

package io.github.crisacm.salem.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScreenNavigationTest {
    @Test
    fun opensAndClosesAScreen() {
        val navigation = ScreenNavigation()
        assertNull(navigation.current)
        navigation.open(AppScreen.ABOUT)
        assertEquals(AppScreen.ABOUT, navigation.current)
        navigation.close(AppScreen.ABOUT)
        assertNull(navigation.current)
    }

    @Test
    fun theLastScreenOpenedIsTheOneShown() {
        val navigation = ScreenNavigation()
        navigation.open(AppScreen.SETTINGS)
        navigation.open(AppScreen.ABOUT)
        assertEquals(AppScreen.ABOUT, navigation.current)
    }

    @Test
    fun closingTheTopScreenShowsTheOneBelow() {
        val navigation = ScreenNavigation()
        navigation.open(AppScreen.SETTINGS)
        navigation.open(AppScreen.ABOUT)
        navigation.close(AppScreen.ABOUT)
        assertEquals(AppScreen.SETTINGS, navigation.current)
        navigation.close(AppScreen.SETTINGS)
        assertNull(navigation.current)
    }

    @Test
    fun closingAScreenThatIsBelowKeepsTheTopOne() {
        val navigation = ScreenNavigation()
        navigation.open(AppScreen.ABOUT)
        navigation.open(AppScreen.INTRO)
        navigation.close(AppScreen.ABOUT)
        assertEquals(AppScreen.INTRO, navigation.current)
        navigation.close(AppScreen.INTRO)
        assertNull(navigation.current)
    }

    @Test
    fun openingAScreenAlreadyOpenDoesNotRepeatIt() {
        val navigation = ScreenNavigation()
        navigation.open(AppScreen.ABOUT)
        navigation.open(AppScreen.ABOUT)
        navigation.close(AppScreen.ABOUT)
        assertNull(navigation.current)
    }

    @Test
    fun openingAScreenThatIsBelowMovesItToTheTop() {
        val navigation = ScreenNavigation()
        navigation.open(AppScreen.ABOUT)
        navigation.open(AppScreen.INTRO)
        navigation.open(AppScreen.ABOUT)
        assertEquals(AppScreen.ABOUT, navigation.current)
        assertEquals(listOf(AppScreen.INTRO, AppScreen.ABOUT), navigation.openScreens)
    }
}
