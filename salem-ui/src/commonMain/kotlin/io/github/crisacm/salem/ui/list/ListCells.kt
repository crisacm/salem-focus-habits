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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.platform.gui.FontAwesome
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.no
import io.github.crisacm.salem.ui.resources.skip_day
import io.github.crisacm.salem.ui.resources.unknown
import io.github.crisacm.salem.ui.resources.yes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The colours a cell uses: the habit colour, a dim and a medium one, and the colour behind it. */
class CellColors(val habit: Color, val low: Color, val medium: Color, val background: Color) {
    fun of(tone: CellTone) = when (tone) {
        CellTone.HABIT -> habit
        CellTone.LOW -> low
        CellTone.MEDIUM -> medium
    }
}

/** The width of a day column and the height of a row of cells; the same 48 dp as `checkmarkWidth` in the Android resources. */
val CheckmarkSize = 48.dp

private fun glyphString(glyph: CheckmarkGlyph) = when (glyph) {
    CheckmarkGlyph.CHECK -> FontAwesome.CHECK
    CheckmarkGlyph.TIMES -> FontAwesome.TIMES
    CheckmarkGlyph.SKIPPED -> FontAwesome.SKIPPED
    CheckmarkGlyph.QUESTION -> FontAwesome.QUESTION
}

private fun valueDescription(value: Int): StringResource = when (value) {
    Entry.YES_MANUAL, Entry.YES_AUTO -> Res.string.yes
    Entry.SKIP -> Res.string.skip_day
    Entry.NO -> Res.string.no
    else -> Res.string.unknown
}

/**
 * The cell of a yes or no habit for one day. A tap toggles the value and a long press edits it, or the
 * opposite when [shortToggle] is on. [onToggle] receives the next value.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckmarkCell(
    value: Int,
    notes: String,
    colors: CellColors,
    questionMarksEnabled: Boolean,
    skipEnabled: Boolean,
    shortToggle: Boolean,
    onToggle: (Int) -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val look = checkmarkLook(value, questionMarksEnabled)
    val glyphs = rememberFontAwesome()
    val haptic = LocalHapticFeedback.current
    val color = colors.of(look.tone)
    val description = stringResource(valueDescription(value))
    val glyph = glyphString(look.glyph)
    val toggle = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onToggle(Entry.nextToggleValue(value, skipEnabled, questionMarksEnabled))
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(CheckmarkSize)
            .semantics { contentDescription = description }
            .testTag("checkmark_$value")
            .combinedClickable(
                onClick = if (shortToggle) toggle else onEdit,
                onLongClick = if (shortToggle) onEdit else toggle
            )
            .drawBehind {
                if (notes.isNotBlank()) drawCircle(colors.habit, radius = 8f, center = Offset(size.width - 12.dp.toPx(), 12.dp.toPx()))
            }
    ) {
        if (look.hollow) {
            // The automatic yes is an outline: the stroked glyph with the background colour inside.
            Text(glyph, style = TextStyle(color = color, fontFamily = glyphs, fontSize = look.sizeSp.sp, drawStyle = Stroke(5f)))
        }
        Text(
            glyph,
            style = TextStyle(
                color = if (look.hollow) colors.background else color,
                fontFamily = glyphs,
                fontSize = look.sizeSp.sp
            )
        )
    }
}

/** The cell of a numerical habit for one day: the number, with its units below. A tap or a long press edits it. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberCell(
    value: Double,
    completed: Boolean,
    units: String,
    notes: String,
    colors: CellColors,
    questionMarksEnabled: Boolean,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glyphs = rememberFontAwesome()
    val condensed = rememberCondensedFonts()
    val color = colors.of(numberTone(value, completed))
    val display = numberDisplay(value, questionMarksEnabled)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(CheckmarkSize)
            .combinedClickable(onClick = onEdit, onLongClick = onEdit)
            .drawBehind {
                if (notes.isNotBlank()) drawCircle(colors.habit, radius = 8f, center = Offset(size.width - 12.dp.toPx(), 12.dp.toPx()))
            }
    ) {
        when (display) {
            NumberDisplay.Skipped -> Text(FontAwesome.SKIPPED, color = color, fontFamily = glyphs, fontSize = 14.sp)
            NumberDisplay.Question -> Text(FontAwesome.QUESTION, color = color, fontFamily = glyphs, fontSize = 12.sp)
            is NumberDisplay.Value -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(display.text, color = color, fontFamily = condensed.bold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (units.isNotBlank()) {
                    Text(
                        units,
                        color = color,
                        fontFamily = condensed.regular,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.size(width = 43.dp, height = 16.dp)
                    )
                }
            }
        }
    }
}

/** The ring that shows the score of a habit, filled in steps of [precision] of a turn (a sixteenth by default). */
@Composable
fun ScoreRing(
    percentage: Float,
    color: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
    diameter: Dp = 15.dp,
    thickness: Dp = 3.dp,
    precision: Float = RING_PRECISION
) {
    val sweep = ringSweepAngle(percentage, precision)
    Box(
        modifier = modifier
            .size(diameter)
            .drawBehind {
                val thickness = thickness.toPx()
                val inset = thickness / 2
                val arcSize = androidx.compose.ui.geometry.Size(size.width - thickness, size.height - thickness)
                val topLeft = Offset(inset, inset)
                drawArc(inactiveColor, -90f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(thickness))
                drawArc(color, -90f, sweep, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(thickness))
            }
    )
}
