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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.platform.gui.Canvas
import io.github.crisacm.salem.platform.gui.View
import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.crisacm.salem.platform.gui.Color as CoreColor

@OptIn(ExperimentalTestApi::class)
class CoreCanvasViewTest {
    /** Paints everything blue and cuts a hole in the middle. */
    private val bluePlate = object : View {
        override fun draw(canvas: Canvas) {
            canvas.setColor(CoreColor(0.0, 0.0, 1.0, 1.0))
            canvas.fill()
            canvas.clearCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 10.0)
        }

        override fun onClick(x: Double, y: Double) {}

        override fun onLongClick(x: Double, y: Double) {}
    }

    @Test
    fun theHoleShowsWhatIsBehindTheViewAndDoesNotErase() = runComposeUiTest {
        setContent {
            Box(Modifier.size(40.dp).background(Color.Red)) {
                CoreCanvasView(bluePlate, FontFamily.Default, Modifier.size(40.dp))
            }
        }
        val pixels = onRoot().captureToImage().toPixelMap()
        val middle = pixels.width / 2
        assertEquals(Color.Red, pixels[middle, middle].copy(alpha = 1f), "the hole shows the red behind the view")
        assertEquals(1f, pixels[middle, middle].alpha)
        assertEquals(Color.Blue, pixels[1, 1].copy(alpha = 1f))
    }
}
