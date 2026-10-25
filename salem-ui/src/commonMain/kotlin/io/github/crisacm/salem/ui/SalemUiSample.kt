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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.ui.views.RingChart
import io.github.crisacm.salem.platform.gui.FontAwesome
import io.github.crisacm.salem.ui.canvas.CoreCanvasView
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.fontawesome
import org.jetbrains.compose.resources.Font
import io.github.crisacm.salem.platform.gui.Color as CoreColor

/**
 * A screen to check that the shared UI draws a chart of salem-core on the platform: a ring with a check, drawn
 * by [CoreCanvasView]. The iOS app shows it until the first real screen is moved here.
 */
@Composable
fun SalemUiSample() {
    val fontAwesome = FontFamily(Font(Res.font.fontawesome))
    val theme = remember { LightTheme() }
    val ring = remember {
        RingChart(
            color = theme.color(7),
            percentage = 0.75,
            thickness = 12.0,
            inactiveColor = CoreColor.BLACK.withAlpha(0.15),
            text = FontAwesome.CHECK,
            isFontAwesome = true,
            textSize = 64.0
        )
    }
    Box(Modifier.fillMaxSize().background(Color(0xFFF4F4F4)), contentAlignment = Alignment.Center) {
        CoreCanvasView(ring, fontAwesome, Modifier.size(200.dp))
    }
}
