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

package io.github.crisacm.salem.core.ui.views

import io.github.crisacm.salem.platform.gui.Canvas

// The y of `Canvas.drawText` is about the middle of the lower case letters; this is how far that is above the baseline.
private const val BASELINE_TO_MIDDLE = 0.3

/**
 * Draws [text] with its baseline at [baselineY], as the old chart Views (which used `android.graphics.Canvas`)
 * positioned it, for the font size [fontSize] that is set on the canvas.
 */
internal fun Canvas.drawTextAtBaseline(text: String, x: Double, baselineY: Double, fontSize: Double) {
    drawText(text, x, baselineY - BASELINE_TO_MIDDLE * fontSize)
}
