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

import androidx.compose.ui.graphics.Color
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.ui.views.PureBlackTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.crisacm.salem.platform.gui.Color as CoreColor

class SalemColorSchemeTest {
    @Test
    fun coreColorConvertsToCompose() {
        assertEquals(Color(0xFFF4F4F4), CoreColor(0xF4F4F4).toCompose())
        assertEquals(Color(0f, 0f, 0f, 0f), CoreColor.TRANSPARENT.toCompose())
    }

    @Test
    fun lightSchemeUsesThemeColors() {
        val theme = LightTheme()
        val scheme = theme.toColorScheme()
        assertEquals(theme.appBackgroundColor.toCompose(), scheme.background)
        assertEquals(theme.cardBackgroundColor.toCompose(), scheme.surface)
        assertEquals(theme.highContrastTextColor.toCompose(), scheme.onSurface)
        assertEquals(theme.color(11).toCompose(), scheme.primary)
    }

    @Test
    fun darkAndPureBlackDifferOnBackgroundOnly() {
        val dark = DarkTheme().toColorScheme()
        val pureBlack = PureBlackTheme().toColorScheme()
        assertEquals(Color.Black, pureBlack.background)
        assertTrue(dark.background != pureBlack.background)
        assertEquals(dark.onSurface, pureBlack.onSurface)
    }

    @Test
    fun darkThemesProduceDarkScheme() {
        assertTrue(DarkTheme().toColorScheme().background.luminanceIsLow())
        assertTrue(!LightTheme().toColorScheme().background.luminanceIsLow())
    }

    private fun Color.luminanceIsLow() = red * 0.21f + green * 0.72f + blue * 0.07f < 0.5f
}
