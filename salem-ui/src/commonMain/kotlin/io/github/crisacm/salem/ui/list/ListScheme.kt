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

package io.github.crisacm.salem.ui.list

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.PureBlackTheme
import io.github.crisacm.salem.core.ui.views.Theme

private val LightListScheme = lightColorScheme(
    primary = Color(0xFF1C5BC0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE3F9),
    onPrimaryContainer = Color(0xFF0A2B6B),
    secondaryContainer = Color(0xFFDDE3F9),
    onSecondaryContainer = Color(0xFF151B2C),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF45464F),
    surfaceContainer = Color(0xFFEFEDF4),
    surfaceContainerHigh = Color(0xFFE9E7EF),
    surfaceContainerHighest = Color(0xFFE4E1E9),
    outline = Color(0xFF767680)
)

private val DarkListScheme = darkColorScheme(
    primary = Color(0xFFB8C3FF),
    onPrimary = Color(0xFF00177B),
    primaryContainer = Color(0xFF3B45C6),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF3B45C6),
    onSecondaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF121316),
    onBackground = Color(0xFFE4E1E9),
    surface = Color(0xFF121316),
    onSurface = Color(0xFFE4E1E9),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC7C5D0),
    surfaceContainer = Color(0xFF1F1F25),
    surfaceContainerHigh = Color(0xFF292A2F),
    surfaceContainerHighest = Color(0xFF34343A),
    outline = Color(0xFF90909A)
)

private val BlackListScheme = DarkListScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainer = Color(0xFF0E0E10),
    surfaceContainerHigh = Color(0xFF1C1C1F),
    surfaceContainerHighest = Color(0xFF28282C)
)

/**
 * The Material 3 colours of the habit list screen for a theme: the lavender light scheme of the design, and the
 * dark one (or the black one, for the pure black theme). The cards take their own tones from the habit ([habitTones]).
 */
fun Theme.toListColorScheme(): ColorScheme = when (this) {
    is PureBlackTheme -> BlackListScheme
    is DarkTheme -> DarkListScheme
    else -> LightListScheme
}

/** Whether the list uses its dark tones for a theme. */
fun Theme.isDarkList(): Boolean = this is DarkTheme
