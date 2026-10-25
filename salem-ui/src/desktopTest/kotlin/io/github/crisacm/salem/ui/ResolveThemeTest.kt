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

package io.github.crisacm.salem.ui.compose

import io.github.crisacm.salem.core.preferences.MemoryStorage
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.ui.views.PureBlackTheme
import kotlin.test.Test
import kotlin.test.assertTrue

class ResolveThemeTest {
    private fun prefs(theme: Int, pureBlack: Boolean = false) = Preferences(MemoryStorage()).also {
        it.theme = theme
        it.isPureBlackEnabled = pureBlack
    }

    @Test
    fun automaticFollowsTheSystem() {
        assertTrue(resolveTheme(prefs(ThemeSwitcher.THEME_AUTOMATIC), systemIsDark = false) is LightTheme)
        assertTrue(resolveTheme(prefs(ThemeSwitcher.THEME_AUTOMATIC), systemIsDark = true) is DarkTheme)
    }

    @Test
    fun explicitChoiceBeatsTheSystem() {
        assertTrue(resolveTheme(prefs(ThemeSwitcher.THEME_DARK), systemIsDark = false) is DarkTheme)
        assertTrue(resolveTheme(prefs(ThemeSwitcher.THEME_LIGHT), systemIsDark = true) is LightTheme)
    }

    @Test
    fun pureBlackOnlyAppliesToTheDarkTheme() {
        assertTrue(resolveTheme(prefs(ThemeSwitcher.THEME_DARK, true), systemIsDark = false) is PureBlackTheme)
        assertTrue(resolveTheme(prefs(ThemeSwitcher.THEME_LIGHT, true), systemIsDark = true) is LightTheme)
    }
}
