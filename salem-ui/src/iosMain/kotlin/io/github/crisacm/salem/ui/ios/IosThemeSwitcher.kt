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

package io.github.crisacm.salem.ui.ios

import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.ui.views.PureBlackTheme
import io.github.crisacm.salem.core.ui.views.Theme
import platform.UIKit.UIScreen
import platform.UIKit.UIUserInterfaceStyle

/**
 * The theme of the iOS app: it follows the system appearance unless the user chose one (the preference is shared
 * with the rest of the app). It only keeps the current [Theme]; the colours are applied by the Compose theme.
 */
class IosThemeSwitcher(preferences: Preferences) : ThemeSwitcher(preferences) {
    override var currentTheme: Theme? = null
        private set

    /** Whether the system appearance is dark: the screen sets it from what Compose reports for its window. */
    var systemIsDark: Boolean =
        UIScreen.mainScreen.traitCollection.userInterfaceStyle == UIUserInterfaceStyle.UIUserInterfaceStyleDark

    init {
        apply()
    }

    override fun applyDarkTheme() {
        currentTheme = DarkTheme()
    }

    override fun applyLightTheme() {
        currentTheme = LightTheme()
    }

    override fun applyPureBlackTheme() {
        currentTheme = PureBlackTheme()
    }

    override fun getSystemTheme(): Int = if (systemIsDark) THEME_DARK else THEME_LIGHT
}
