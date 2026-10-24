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

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import io.github.crisacm.salem.platform.gui.AndroidCanvas
import io.github.crisacm.salem.platform.gui.View

/**
 * Draws a [View] of salem-core through the multiplatform `Canvas`, with the same [AndroidCanvas] the chart
 * Views used, so text, fonts and sizes look the same. [beforeDraw] runs in the draw phase, so the state it
 * reads redraws the view when it changes.
 */
@Composable
fun CoreView(view: View, modifier: Modifier = Modifier, beforeDraw: DrawScope.() -> Unit = {}) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val canvas = remember { AndroidCanvas() }
    Canvas(modifier) {
        beforeDraw()
        drawIntoCanvas { target ->
            canvas.context = context
            canvas.innerCanvas = target.nativeCanvas
            canvas.innerWidth = size.width.toInt()
            canvas.innerHeight = size.height.toInt()
            canvas.innerDensity = density.toDouble()
            view.draw(canvas)
        }
    }
}
