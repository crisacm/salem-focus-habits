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

import androidx.compose.ui.graphics.Color
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.PureBlackTheme
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.ui.compose.toCompose

private const val BAR_PALETTE_INDEX = 17

/**
 * The colours of the list screen for a theme: the values that the Android themes give to the attributes the
 * screen reads (`windowBackgroundColor`, `cardBgColor`, `contrast40`...), so a platform without Android resources
 * shows the same screen. The light theme uses the habit palette colour for the bar, the dark ones a dark grey or black.
 */
fun listScreenColorsFor(theme: Theme): ListScreenColors = when (theme) {
    is PureBlackTheme -> ListScreenColors(
        windowBackground = Color(0xFF000000),
        barColor = Color(0xFF000000),
        onBarColor = Color.White,
        headerBackground = Color(0xFF000000),
        headerText = Color(0xFF9E9E9E),
        list = ListColors(
            card = Color(0xFF000000),
            highlighted = Color(0xFF000000),
            low = Color(0xFF424242),
            medium = Color(0xFF9E9E9E),
            ringTrack = Color(0xFFEEEEEE).copy(alpha = 0.15f)
        )
    )
    is DarkTheme -> ListScreenColors(
        windowBackground = Color(0xFF212121),
        barColor = Color(0xFF101010),
        onBarColor = Color.White,
        headerBackground = Color(0xFF212121),
        headerText = Color(0xFF9E9E9E),
        list = ListColors(
            card = Color(0xFF303030),
            highlighted = Color(0xFF424242),
            low = Color(0xFF525252),
            medium = Color(0xFF9E9E9E),
            ringTrack = Color(0xFFF5F5F5).copy(alpha = 0.15f)
        )
    )
    else -> ListScreenColors(
        windowBackground = Color(0xFFEEEEEE),
        barColor = theme.color(PaletteColor(BAR_PALETTE_INDEX)).toCompose(),
        onBarColor = Color.White,
        headerBackground = Color(0xFFEEEEEE),
        headerText = Color(0xFF9E9E9E),
        list = ListColors(
            card = Color(0xFFFAFAFA),
            highlighted = Color(0xFFF5F5F5),
            low = Color(0xFFD8D8D8),
            medium = Color(0xFF9E9E9E),
            ringTrack = Color(0xFF424242).copy(alpha = 0.15f)
        )
    )
}
