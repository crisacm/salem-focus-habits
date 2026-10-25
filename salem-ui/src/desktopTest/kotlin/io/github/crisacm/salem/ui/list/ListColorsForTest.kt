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
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.ui.views.PureBlackTheme
import io.github.crisacm.salem.ui.compose.toCompose
import kotlin.test.Test
import kotlin.test.assertEquals

/** The colours match the values the Android themes give to the attributes the list screen reads. */
class ListColorsForTest {
    @Test
    fun lightTheme() {
        val colors = listScreenColorsFor(LightTheme())
        assertEquals(Color(0xFFEEEEEE), colors.windowBackground)
        assertEquals(Color(0xFFEEEEEE), colors.headerBackground)
        assertEquals(Color(0xFF9E9E9E), colors.headerText)
        assertEquals(Color(0xFFFAFAFA), colors.list.card)
        assertEquals(Color(0xFFF5F5F5), colors.list.highlighted)
        assertEquals(Color(0xFFD8D8D8), colors.list.low)
        assertEquals(Color(0xFF9E9E9E), colors.list.medium)
        assertEquals(Color(0xFF424242).copy(alpha = 0.15f), colors.list.ringTrack)
        // The bar is the habit palette colour 17 of the theme
        assertEquals(LightTheme().color(17).toCompose(), colors.barColor)
        assertEquals(Color.White, colors.onBarColor)
    }

    @Test
    fun darkTheme() {
        val colors = listScreenColorsFor(DarkTheme())
        assertEquals(Color(0xFF212121), colors.windowBackground)
        assertEquals(Color(0xFF101010), colors.barColor)
        assertEquals(Color(0xFF303030), colors.list.card)
        assertEquals(Color(0xFF424242), colors.list.highlighted)
        assertEquals(Color(0xFF525252), colors.list.low)
        assertEquals(Color(0xFFF5F5F5).copy(alpha = 0.15f), colors.list.ringTrack)
    }

    @Test
    fun pureBlackTheme() {
        val colors = listScreenColorsFor(PureBlackTheme())
        assertEquals(Color.Black, colors.windowBackground)
        assertEquals(Color.Black, colors.barColor)
        assertEquals(Color.Black, colors.list.card)
        assertEquals(Color(0xFF424242), colors.list.low)
        assertEquals(Color(0xFFEEEEEE).copy(alpha = 0.15f), colors.list.ringTrack)
    }
}
