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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.PureBlackTheme
import io.github.crisacm.salem.core.ui.views.Theme

private val LightExpressiveScheme = lightColorScheme(
    primary = Color(0xFF1B5FC1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE8FB),
    onPrimaryContainer = Color(0xFF052654),
    secondaryContainer = Color(0xFFE1E1FE),
    onSecondaryContainer = Color(0xFF010055),
    background = Color(0xFFFBF8FD),
    onBackground = Color(0xFF131519),
    surface = Color(0xFFFBF8FD),
    onSurface = Color(0xFF131519),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF434549),
    surfaceContainer = Color(0xFFF1EEF6),
    surfaceContainerHigh = Color(0xFFEDE9F1),
    surfaceContainerHighest = Color(0xFFE3E0E8),
    outline = Color(0xFF79747E)
)

private val DarkExpressiveScheme = darkColorScheme(
    primary = Color(0xFFAAC7FF),
    onPrimary = Color(0xFF002A62),
    primaryContainer = Color(0xFF1C3460),
    onPrimaryContainer = Color(0xFFE1EEFF),
    secondaryContainer = Color(0xFF353DBE),
    onSecondaryContainer = Color(0xFFE4E4FF),
    background = Color(0xFF111315),
    onBackground = Color(0xFFE9E8EF),
    surface = Color(0xFF111315),
    onSurface = Color(0xFFE9E8EF),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC9C9CD),
    surfaceContainer = Color(0xFF1F1E25),
    surfaceContainerHigh = Color(0xFF2B2A31),
    surfaceContainerHighest = Color(0xFF2F3137),
    outline = Color(0xFF938F99)
)

private val BlackExpressiveScheme = DarkExpressiveScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainer = Color(0xFF0E0E10),
    surfaceContainerHigh = Color(0xFF1C1C1F),
    surfaceContainerHighest = Color(0xFF28282C)
)

/**
 * The Material 3 colours of the Expressive screens (the habit list and the editor) for a theme: the lavender light
 * scheme of the design, and the dark one (or the black one, for the pure black theme). The primary colours (blue)
 * paint the editor; the secondary container (indigo in the dark scheme) the button and the day of the list.
 */
fun Theme.toExpressiveColorScheme(): ColorScheme = when (this) {
    is PureBlackTheme -> BlackExpressiveScheme
    is DarkTheme -> DarkExpressiveScheme
    else -> LightExpressiveScheme
}

/** Whether the Expressive screens use their dark tones for a theme. */
fun Theme.usesDarkTones(): Boolean = this is DarkTheme
