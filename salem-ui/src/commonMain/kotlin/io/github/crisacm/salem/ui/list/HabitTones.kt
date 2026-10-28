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
import androidx.compose.ui.graphics.lerp
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.ui.compose.toCompose
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

/**
 * The colours of one habit card: a pale (or deep, in the dark theme) [container] for the card, a strong [accent] for the
 * filled cells, the ring and the icon, a [soft] fill for the cells that are not done (and the track of the ring), and the
 * colours of the text and of the glyphs on each of them. [chip] is the fill of the small round buttons on the card,
 * [muted] the softer colour of the bars of a chart that are not the highlighted one and [emphasis] the colour of the
 * figures that stand out on a neutral card.
 */
class HabitTones(
    val container: Color,
    val onContainer: Color,
    val accent: Color,
    val onAccent: Color,
    val soft: Color,
    val onSoft: Color,
    val ringTrack: Color,
    val chip: Color,
    val muted: Color,
    val emphasis: Color
)

private fun argb(hex: Long) = Color(0xFF000000 or hex)

/** The measured colours of a family of the design: the container, the text on it, the accent, the glyph on it and the soft fill. */
private class Family(container: Long, onContainer: Long, accent: Long, onAccent: Long, soft: Long) {
    val container = argb(container)
    val onContainer = argb(onContainer)
    val accent = argb(accent)
    val onAccent = argb(onAccent)
    val soft = argb(soft)
}

// The four families of the design (blue, pink, orange and purple habits), as drawn in the mockups
private val BlueLight = Family(0xDDE8FB, 0x0A2958, 0x1B5FC1, 0xFFFFFF, 0xF5F8FE)
private val BlueDark = Family(0x1C3460, 0xDBEEFF, 0xAAC7FF, 0x012C62, 0x132340)
private val PinkLight = Family(0xFEDCE8, 0x4C0023, 0xB5135A, 0xFFFFFF, 0xFEF7F9)
private val PinkDark = Family(0x5E1136, 0xFFD8F0, 0xFFB2CC, 0x560228, 0x400C24)
private val OrangeLight = Family(0xFFE2C5, 0x3F1F00, 0xA95500, 0xFFFFFF, 0xFFF8EF)
private val OrangeDark = Family(0x4E2E07, 0xFFE6C1, 0xFFB86C, 0x4F2000, 0x361F04)
private val PurpleLight = Family(0xEAE0FF, 0x25124D, 0x6A40B4, 0xFFFFFF, 0xFAF6FF)
private val PurpleDark = Family(0x3D257F, 0xEEE1FF, 0xD0BCFF, 0x250F5D, 0x281A58)

/** The palette colours (indexes of `PaletteColor`) that have a family drawn in the design: blue, pink, orange and purple. */
private fun familyOf(paletteIndex: Int, dark: Boolean): Family? = when (paletteIndex) {
    11 -> if (dark) BlueDark else BlueLight
    15 -> if (dark) PinkDark else PinkLight
    2 -> if (dark) OrangeDark else OrangeLight
    13 -> if (dark) PurpleDark else PurpleLight
    else -> null
}

/** The grey that an archived habit takes. */
const val ARCHIVED_TONES = -1

/**
 * The tones of a card for a habit whose colour is the palette colour [paletteIndex] (the colour of the light theme,
 * whatever theme is shown), or [ARCHIVED_TONES] for a grey one. The four colours of the design have the exact tones of the
 * mockups; any other colour gets tones made the same way (the same lightness and a similar chroma for each role).
 */
fun habitTones(paletteIndex: Int, dark: Boolean): HabitTones {
    val family = familyOf(paletteIndex, dark)
    val colors = if (family != null) {
        listOf(family.container, family.onContainer, family.accent, family.onAccent, family.soft)
    } else {
        generatedTones(paletteIndex, dark)
    }
    val (container, onContainer, accent, onAccent, soft) = colors
    return HabitTones(
        container = container,
        onContainer = onContainer,
        accent = accent,
        onAccent = onAccent,
        soft = soft,
        // What is written on a soft cell and not enough to read twice (an empty day) is fainter than the text of the card
        onSoft = lerp(soft, onContainer, 0.6f),
        ringTrack = soft,
        chip = if (dark) Color.White.copy(alpha = 0.12f) else soft,
        muted = lerp(container, accent, 0.30f),
        emphasis = lerp(accent, onContainer, if (dark) 0.27f else 0.5f)
    )
}

private val lightPalette = LightTheme()

private fun generatedTones(paletteIndex: Int, dark: Boolean): List<Color> {
    val grey = paletteIndex == ARCHIVED_TONES
    val seed = if (grey) Color.Gray else lightPalette.color(paletteIndex).toCompose()
    val (seedChroma, hue) = oklchOf(seed)
    val scale = if (grey) 0.0 else 1.0
    return if (dark) {
        listOf(
            toneColor(hue, 0.10 * scale, 22.0),
            toneColor(hue, 0.045 * scale, 92.0),
            toneColor(hue, 0.10 * scale, 80.0),
            toneColor(hue, 0.10 * scale, 16.0),
            toneColor(hue, 0.075 * scale, 14.0)
        )
    } else {
        listOf(
            toneColor(hue, 0.040 * scale, 91.5),
            toneColor(hue, 0.090 * scale, 14.5),
            toneColor(hue, seedChroma * 0.9 * scale, 41.0),
            Color.White,
            toneColor(hue, 0.011 * scale, 97.7)
        )
    }
}

// A colour as lightness (the tone of Material 3, which is the lightness of CIE Lab), chroma and hue of the Oklab space.

private fun toLinear(channel: Float): Double {
    val c = channel.toDouble()
    return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
}

private fun toSrgb(linear: Double): Float {
    val c = linear.coerceIn(0.0, 1.0)
    return (if (c <= 0.0031308) 12.92 * c else 1.055 * c.pow(1 / 2.4) - 0.055).toFloat()
}

private fun cubeRoot(x: Double) = if (x < 0) -(-x).pow(1.0 / 3) else x.pow(1.0 / 3)

/** The chroma and the hue (in degrees) of [color] in Oklab. */
private fun oklchOf(color: Color): Pair<Double, Double> {
    val r = toLinear(color.red)
    val g = toLinear(color.green)
    val b = toLinear(color.blue)
    val l = cubeRoot(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b)
    val m = cubeRoot(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b)
    val s = cubeRoot(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b)
    val a = 1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s
    val bb = 0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s
    val hue = (atan2(bb, a) * 180 / PI).let { if (it < 0) it + 360 else it }
    return hypot(a, bb) to hue
}

private fun luminanceOfTone(tone: Double): Double {
    val f = (tone + 16) / 116
    val cubed = f * f * f
    return if (cubed > 216.0 / 24389) cubed else (116 * f - 16) / (24389.0 / 27)
}

private fun linearRgb(lightness: Double, a: Double, b: Double): DoubleArray {
    val l = (lightness + 0.3963377774 * a + 0.2158037573 * b).pow(3)
    val m = (lightness - 0.1055613458 * a - 0.0638541728 * b).pow(3)
    val s = (lightness - 0.0894841775 * a - 1.2914855480 * b).pow(3)
    return doubleArrayOf(
        4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
        -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
        -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s
    )
}

private fun luminanceOf(rgb: DoubleArray) = 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]

/**
 * The colour with the given [hue] (degrees), [chroma] and [tone] (0 to 100, as the lightness of Material 3). If the chroma does
 * not exist at that tone (a pale or a dark yellow cannot be very colourful) it is lowered until the colour fits in sRGB.
 */
private fun toneColor(hue: Double, chroma: Double, tone: Double): Color {
    val target = luminanceOfTone(tone)
    val radians = hue * PI / 180
    var c = chroma
    while (true) {
        var low = 0.0
        var high = 1.0
        repeat(40) {
            val mid = (low + high) / 2
            val rgb = linearRgb(mid, c * cos(radians), c * sin(radians))
            if (luminanceOf(rgb) < target) low = mid else high = mid
        }
        val rgb = linearRgb(low, c * cos(radians), c * sin(radians))
        if (c <= 0.0001 || rgb.all { it in -0.0001..1.0001 }) {
            return Color(toSrgb(rgb[0]), toSrgb(rgb[1]), toSrgb(rgb[2]))
        }
        c -= 0.002
    }
}
