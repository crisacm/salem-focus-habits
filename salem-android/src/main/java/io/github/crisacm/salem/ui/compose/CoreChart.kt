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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import io.github.crisacm.salem.platform.gui.AndroidCanvas
import io.github.crisacm.salem.platform.gui.DataView

/**
 * Draws a chart of salem-core (a [DataView]) through the multiplatform `Canvas`, scrolls its data with a
 * horizontal drag and a fling, and passes taps and long presses to it in dp. It draws with the same
 * [AndroidCanvas] the chart Views used, so text, fonts and sizes look the same.
 */
@Composable
fun CoreChart(chart: DataView, scroll: ChartScrollState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val canvas = remember { AndroidCanvas() }
    val dragState = rememberDraggableState { delta ->
        scroll.setColumnWidth((chart.dataColumnWidth * density).toFloat())
        scroll.dragBy(delta)
    }
    Canvas(
        modifier = modifier
            .draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                // The old scroller flung at half of the speed of the finger.
                onDragStopped = { velocity -> scroll.fling(velocity / 2) }
            )
            .pointerInput(chart) {
                detectTapGestures(
                    onTap = { chart.onClick(it.x / density.toDouble(), it.y / density.toDouble()) },
                    onLongPress = { chart.onLongClick(it.x / density.toDouble(), it.y / density.toDouble()) }
                )
            }
    ) {
        // Reading the offset here redraws the chart when it changes.
        chart.dataOffset = scroll.dataOffset
        drawIntoCanvas { target ->
            canvas.context = context
            canvas.innerCanvas = target.nativeCanvas
            canvas.innerWidth = size.width.toInt()
            canvas.innerHeight = size.height.toInt()
            canvas.innerDensity = density.toDouble()
            chart.draw(canvas)
        }
    }
}
