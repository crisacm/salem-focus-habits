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

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.platform.gui.Canvas
import io.github.crisacm.salem.platform.gui.Font
import io.github.crisacm.salem.platform.gui.Image
import io.github.crisacm.salem.platform.gui.TextAlign
import androidx.compose.ui.graphics.Color as ComposeColor
import io.github.crisacm.salem.platform.gui.Color as CoreColor

/**
 * The [Canvas] of salem-core over a Compose [DrawScope], for iOS and for the shared screens of Android. All
 * sizes and coordinates of the core are in dp; the scope is in pixels, so they are multiplied by its density,
 * as `AndroidCanvas` does. Set [scope] before each draw. [clearCircle] cuts a hole with [BlendMode.Clear], so
 * the caller must draw on its own transparent layer (see [CoreChart]).
 *
 * [regularFamily] and [boldFamily] are the families of [Font.REGULAR] and [Font.BOLD]; [fontAwesomeFamily]
 * holds the glyphs of `FontAwesome`.
 */
class DrawScopeCanvas(
    private val textMeasurer: TextMeasurer,
    private val fontAwesomeFamily: FontFamily,
    private val regularFamily: FontFamily = FontFamily.Default,
    private val boldFamily: FontFamily = FontFamily.Default,
    private val image: (() -> Image)? = null
) : Canvas {

    lateinit var scope: DrawScope

    private var color = ComposeColor.Black
    private var font = Font.REGULAR
    private var fontSize = 12.0
    private var strokeWidth = 1.0
    private var textAlign = TextAlign.CENTER

    private val density get() = scope.density.toDouble()
    private fun Double.px() = (this * density).toFloat()

    private fun CoreColor.toCompose() = ComposeColor(
        red.toFloat().coerceIn(0f, 1f),
        green.toFloat().coerceIn(0f, 1f),
        blue.toFloat().coerceIn(0f, 1f),
        alpha.toFloat().coerceIn(0f, 1f)
    )

    override fun setColor(color: CoreColor) {
        this.color = color.toCompose()
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        scope.drawLine(color, Offset(x1.px(), y1.px()), Offset(x2.px(), y2.px()), strokeWidth.px())
    }

    override fun drawText(text: String, x: Double, y: Double) {
        draw(text, x, y, TextStyle(color = color))
    }

    override fun strokeText(text: String, x: Double, y: Double) {
        draw(text, x, y, TextStyle(color = color, drawStyle = Stroke(strokeWidth.px())))
    }

    private fun style(base: TextStyle): TextStyle = with(scope) {
        base.copy(
            fontSize = fontSize.dp.toSp(),
            fontFamily = when (font) {
                Font.REGULAR -> regularFamily
                Font.BOLD -> boldFamily
                Font.FONT_AWESOME -> fontAwesomeFamily
            },
            fontWeight = if (font == Font.BOLD) FontWeight.Bold else FontWeight.Normal
        )
    }

    private fun measure(text: String, base: TextStyle) =
        textMeasurer.measure(text, style(base), softWrap = false, maxLines = 1, constraints = Constraints())

    private fun draw(text: String, x: Double, y: Double, base: TextStyle) {
        val layout = measure(text, base)
        val width = layout.size.width
        val left = when (textAlign) {
            TextAlign.LEFT -> x.px()
            TextAlign.CENTER -> x.px() - width / 2f
            TextAlign.RIGHT -> x.px() - width
        }
        val top = y.px() - layout.size.height / 2f
        scope.drawText(layout, topLeft = Offset(left, top))
    }

    override fun fillRect(x: Double, y: Double, width: Double, height: Double) {
        scope.drawRect(color, Offset(x.px(), y.px()), Size(width.px(), height.px()))
    }

    override fun fillRoundRect(x: Double, y: Double, width: Double, height: Double, cornerRadius: Double) {
        scope.drawRoundRect(
            color,
            Offset(x.px(), y.px()),
            Size(width.px(), height.px()),
            CornerRadius(cornerRadius.px())
        )
    }

    override fun drawRect(x: Double, y: Double, width: Double, height: Double) {
        scope.drawRect(color, Offset(x.px(), y.px()), Size(width.px(), height.px()), style = Stroke(strokeWidth.px()))
    }

    override fun getHeight(): Double = scope.size.height / density

    override fun getWidth(): Double = scope.size.width / density

    override fun setFont(font: Font) {
        this.font = font
    }

    override fun setFontSize(size: Double) {
        fontSize = size
    }

    override fun setStrokeWidth(size: Double) {
        strokeWidth = size
    }

    override fun fillArc(centerX: Double, centerY: Double, radius: Double, startAngle: Double, swipeAngle: Double) {
        // The core measures angles counterclockwise; Compose measures them clockwise
        val r = radius.px()
        scope.drawArc(
            color,
            -startAngle.toFloat(),
            -swipeAngle.toFloat(),
            true,
            Offset(centerX.px() - r, centerY.px() - r),
            Size(2 * r, 2 * r)
        )
    }

    override fun fillCircle(centerX: Double, centerY: Double, radius: Double) {
        scope.drawCircle(color, radius.px(), Offset(centerX.px(), centerY.px()))
    }

    override fun clearCircle(centerX: Double, centerY: Double, radius: Double) {
        scope.drawCircle(ComposeColor.Black, radius.px(), Offset(centerX.px(), centerY.px()), blendMode = BlendMode.Clear)
    }

    override fun setTextAlign(align: TextAlign) {
        textAlign = align
    }

    override fun toImage(): Image = image?.invoke() ?: throw UnsupportedOperationException()

    override fun measureText(text: String): Double = measure(text, TextStyle()).size.width / density

    override fun fontSpacing(): Double = measure("M", TextStyle()).size.height / density
}
