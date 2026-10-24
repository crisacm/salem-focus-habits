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

package io.github.crisacm.salem.platform.gui

import android.content.Context
import android.graphics.Canvas

/**
 * A View that draws a [view] of salem-core for a widget. The chart is drawn on a transparent layer of its
 * own and then put over what the widget already holds (its card), so a chart can cut holes in itself with
 * `Canvas.clearCircle` without erasing the card behind it. It does not scroll or take touches: a widget is a
 * picture.
 */
class CoreWidgetView(context: Context, var view: io.github.crisacm.salem.platform.gui.View? = null) :
    android.view.View(context) {

    private val canvas = AndroidCanvas()

    override fun onDraw(target: Canvas) {
        val core = view ?: return
        val layer = target.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas.context = context
        canvas.innerCanvas = target
        canvas.innerWidth = width
        canvas.innerHeight = height
        canvas.innerDensity = resources.displayMetrics.density.toDouble()
        core.draw(canvas)
        target.restoreToCount(layer)
    }
}
