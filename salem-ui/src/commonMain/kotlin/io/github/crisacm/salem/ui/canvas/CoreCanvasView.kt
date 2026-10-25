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

package io.github.crisacm.salem.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.rememberTextMeasurer
import io.github.crisacm.salem.platform.gui.View

/**
 * Draws a [View] of salem-core with a [DrawScopeCanvas]. The view is drawn on a layer of its own, so charts that
 * cut holes in themselves (`Canvas.clearCircle`) do not erase what is behind them. [beforeDraw] runs in the draw
 * phase, so the state it reads redraws the view when it changes. The core views are plain objects, not Compose
 * state: when one of their properties changes, pass a different [version] (a counter, or the data it shows) to
 * draw it again.
 */
@Composable
fun CoreCanvasView(
    view: View,
    fonts: SalemFonts,
    modifier: Modifier = Modifier.fillMaxSize(),
    version: Any? = null,
    beforeDraw: DrawScope.() -> Unit = {}
) {
    val textMeasurer = rememberTextMeasurer()
    val canvas = remember(textMeasurer, fonts) { DrawScopeCanvas(textMeasurer, fonts.fontAwesome, fonts.regular, fonts.bold) }
    Canvas(modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
        version
        beforeDraw()
        canvas.scope = this
        view.draw(canvas)
    }
}
