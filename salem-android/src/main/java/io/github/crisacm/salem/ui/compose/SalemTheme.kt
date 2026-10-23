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

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.ui.views.PureBlackTheme
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.gui.Color as CoreColor

private const val PRIMARY_PALETTE_INDEX = 11

fun CoreColor.toCompose() = Color(red.toFloat(), green.toFloat(), blue.toFloat(), alpha.toFloat())

fun Theme.toColorScheme(): ColorScheme {
    val background = appBackgroundColor.toCompose()
    val surface = cardBackgroundColor.toCompose()
    val onSurface = highContrastTextColor.toCompose()
    val primary = color(PRIMARY_PALETTE_INDEX).toCompose()
    val base = if (this is DarkTheme) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = Color.White,
        background = background,
        onBackground = onSurface,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = headerBackgroundColor.toCompose(),
        onSurfaceVariant = mediumContrastTextColor.toCompose(),
        outline = headerBorderColor.toCompose()
    )
}

@Composable
fun SalemTheme(theme: Theme, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = theme.toColorScheme(), content = content)
}

/** The theme [ThemeSwitcher] would pick for these preferences, without touching any activity. */
fun resolveTheme(preferences: Preferences, systemIsDark: Boolean): Theme {
    val userTheme = preferences.theme
    val night = userTheme == ThemeSwitcher.THEME_DARK ||
        systemIsDark && userTheme == ThemeSwitcher.THEME_AUTOMATIC
    return when {
        !night -> LightTheme()
        preferences.isPureBlackEnabled -> PureBlackTheme()
        else -> DarkTheme()
    }
}

/**
 * Whether the system is in night mode as [io.github.crisacm.salem.activities.AndroidThemeSwitcher] sees
 * it: before Android 10 the activities never follow the system, so neither do the dialogs.
 */
fun systemIsDark(sdkInt: Int, nightMode: Boolean) = sdkInt >= 29 && nightMode

/** [SalemTheme] using the theme chosen in the preferences, for views hosted outside an activity theme switch. */
@Composable
fun SalemTheme(content: @Composable () -> Unit) {
    val preferences = (LocalContext.current.applicationContext as HabitsApplication).component.preferences
    SalemTheme(resolveTheme(preferences, systemIsDark(Build.VERSION.SDK_INT, isSystemInDarkTheme())), content)
}
