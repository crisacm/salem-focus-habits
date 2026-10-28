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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * The colours of one habit card, derived from the colour of the habit as Material 3 does with a seed:
 * a pale (or deep, in the dark theme) [container] for the card, a strong [accent] for the filled cells, the
 * ring and the icon, and a [soft] fill for the cells that are not done.
 */
class HabitTones(
    val container: Color,
    val onContainer: Color,
    val accent: Color,
    val onAccent: Color,
    val soft: Color,
    val onSoft: Color,
    val ringTrack: Color,
    val chip: Color
)

private class Hsl(val hue: Float, val saturation: Float, val lightness: Float)

private fun Color.toHsl(): Hsl {
    val max = max(red, max(green, blue))
    val min = min(red, min(green, blue))
    val lightness = (max + min) / 2
    val delta = max - min
    if (delta == 0f) return Hsl(0f, 0f, lightness)
    val saturation = delta / (1 - abs(2 * lightness - 1))
    val hue = when (max) {
        red -> ((green - blue) / delta).mod(6f)
        green -> (blue - red) / delta + 2
        else -> (red - green) / delta + 4
    } * 60f
    return Hsl(hue, saturation.coerceIn(0f, 1f), lightness)
}

private fun hsl(hue: Float, saturation: Float, lightness: Float): Color {
    val chroma = (1 - abs(2 * lightness - 1)) * saturation
    val section = hue / 60f
    val x = chroma * (1 - abs(section.mod(2f) - 1))
    val (r, g, b) = when (section.toInt()) {
        0 -> Triple(chroma, x, 0f)
        1 -> Triple(x, chroma, 0f)
        2 -> Triple(0f, chroma, x)
        3 -> Triple(0f, x, chroma)
        4 -> Triple(x, 0f, chroma)
        else -> Triple(chroma, 0f, x)
    }
    val m = lightness - chroma / 2
    return Color(r + m, g + m, b + m)
}

/** The tones of a card for a habit of colour [habit]; a grey habit (such as an archived one) gets grey tones. */
fun habitTones(habit: Color, dark: Boolean): HabitTones {
    val base = habit.toHsl()
    // Very bright or very pale habit colours would give washed out tones, so the saturation is kept in a band.
    val saturation = if (base.saturation < 0.08f) 0f else base.saturation.coerceIn(0.45f, 0.95f)
    val hue = base.hue
    return if (dark) {
        val accent = hsl(hue, saturation * 0.9f, 0.80f)
        HabitTones(
            container = hsl(hue, saturation * 0.6f, 0.20f),
            onContainer = hsl(hue, saturation * 0.9f, 0.92f),
            accent = accent,
            onAccent = hsl(hue, saturation * 0.8f, 0.14f),
            soft = hsl(hue, saturation * 0.6f, 0.14f),
            onSoft = hsl(hue, saturation * 0.3f, 0.55f),
            ringTrack = accent.copy(alpha = 0.25f),
            chip = hsl(hue, saturation * 0.4f, 0.32f)
        )
    } else {
        val accent = hsl(hue, saturation * 0.85f, 0.38f)
        HabitTones(
            container = hsl(hue, saturation * 0.85f, 0.91f),
            onContainer = hsl(hue, saturation * 0.8f, 0.16f),
            accent = accent,
            onAccent = Color.White,
            soft = hsl(hue, saturation * 0.6f, 0.97f),
            onSoft = hsl(hue, saturation * 0.3f, 0.55f),
            ringTrack = accent.copy(alpha = 0.18f),
            chip = Color.White.copy(alpha = 0.75f)
        )
    }
}
