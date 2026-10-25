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

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.crisacm.salem.platform.gui.JavaImage
import io.github.crisacm.salem.platform.gui.View
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.fail

private const val SCALE = 2
private val assets = File("../salem-core/assets")

private fun font(name: String, weight: FontWeight = FontWeight.Normal) =
    Font(name, File(assets, "main/fonts/$name.ttf").readBytes(), weight)

private val noto by lazy { FontFamily(font("NotoSans-Regular"), font("NotoSans-Bold", FontWeight.Bold)) }
private val fontAwesome by lazy { FontFamily(font("FontAwesome")) }

fun textMeasurerForTest() = TextMeasurer(createFontFamilyResolver(), Density(SCALE.toFloat()), LayoutDirection.Ltr)

/** Draws on a transparent bitmap of [width] x [height] dp at 2 pixels per dp with a [DrawScopeCanvas]. */
fun render(width: Int, height: Int, block: (DrawScopeCanvas) -> Unit): BufferedImage {
    val density = Density(SCALE.toFloat())
    val canvas = DrawScopeCanvas(textMeasurerForTest(), fontAwesome, noto, noto)
    val bitmap = ImageBitmap(width * SCALE, height * SCALE)
    val size = Size(bitmap.width.toFloat(), bitmap.height.toFloat())
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), size) {
        canvas.scope = this
        block(canvas)
    }
    return bitmap.toBufferedImage()
}

/** Renders [view] and compares it with the image of salem-core. */
fun assertRendersLikeCore(width: Int, height: Int, path: String, view: View, maxDistance: Double = 0.2) {
    val actual = render(width, height) { view.draw(it) }
    val expectedFile = File(assets, "test/$path")
    val failed = File("/tmp/failed-ui/$path")
    if (!expectedFile.exists()) {
        failed.parentFile.mkdirs()
        ImageIO.write(actual, "png", failed)
        fail("Missing $path. Actual image: $failed")
    }
    val expected = ImageIO.read(expectedFile)
    val diff = JavaImage(ImageIO.read(expectedFile))
    diff.diff(JavaImage(actual))
    val distance = diff.averageLuminosity * 100
    if (distance >= maxDistance) {
        failed.parentFile.mkdirs()
        ImageIO.write(actual, "png", failed)
        ImageIO.write(expected, "png", File(failed.path.replace(".png", ".expected.png")))
        fail("Images differ for $path (distance=$distance). Actual image: $failed")
    }
}

private fun ImageBitmap.toBufferedImage(): BufferedImage {
    val pixels = IntArray(width * height)
    readPixels(pixels)
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    image.setRGB(0, 0, width, height, pixels, 0, width)
    return image
}
