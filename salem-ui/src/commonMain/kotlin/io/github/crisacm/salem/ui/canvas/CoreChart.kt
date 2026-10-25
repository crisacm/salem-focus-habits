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

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import io.github.crisacm.salem.platform.gui.DataView
import io.github.crisacm.salem.ui.compose.ChartScrollState

/**
 * Draws a chart of salem-core (a [DataView], see [CoreCanvasView]), scrolls its data with a horizontal drag and a
 * fling, and passes taps and long presses to it in dp. [version] draws it again when the data of the chart changed.
 */
@Composable
fun CoreChart(
    chart: DataView,
    scroll: ChartScrollState,
    fonts: SalemFonts = rememberSalemFonts(),
    modifier: Modifier = Modifier,
    version: Any? = null
) {
    val density = LocalDensity.current.density
    val dragState = rememberDraggableState { delta ->
        scroll.setColumnWidth((chart.dataColumnWidth * density).toFloat())
        scroll.dragBy(delta)
    }
    CoreCanvasView(
        view = chart,
        fonts = fonts,
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
            },
        version = version,
        // Reading the offset here redraws the chart when it changes.
        beforeDraw = { chart.dataOffset = scroll.dataOffset }
    )
}
