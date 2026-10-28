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
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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

/** The height of a day cell; its width is a fifth of the card, so the cells line up with the days of the header. */
val DayCellHeight = 56.dp

/** The rounded square of every day cell. */
val DayCellShape = RoundedCornerShape(22.dp)

private fun glyphString(glyph: CheckmarkGlyph) = when (glyph) {
    CheckmarkGlyph.SKIPPED -> FontAwesome.SKIPPED
    CheckmarkGlyph.QUESTION -> FontAwesome.QUESTION
    else -> ""
}

private fun valueDescription(value: Int): StringResource = when (value) {
    Entry.YES_MANUAL, Entry.YES_AUTO -> Res.string.yes
    Entry.SKIP -> Res.string.skip_day
    Entry.NO -> Res.string.no
    else -> Res.string.unknown
}

private fun fillOf(tone: CellTone, tones: HabitTones) = if (tone == CellTone.HABIT) tones.accent else tones.soft

private fun contentOf(tone: CellTone, tones: HabitTones) = if (tone == CellTone.HABIT) tones.onAccent else tones.onSoft

/** The dot at the top right of a cell that has a note. */
private fun Modifier.noteDot(notes: String, color: Color) = drawBehind {
    if (notes.isNotBlank()) drawCircle(color, radius = 3.dp.toPx(), center = Offset(size.width - 12.dp.toPx(), 12.dp.toPx()))
}

/**
 * The cell of a yes or no habit for one day: a rounded square filled with the accent of the habit when it is
 * done and with its soft tone when it is not. A tap toggles the value and a long press edits it, or the
 * opposite when [shortToggle] is on. [onToggle] receives the next value.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckmarkCell(
    value: Int,
    notes: String,
    tones: HabitTones,
    questionMarksEnabled: Boolean,
    skipEnabled: Boolean,
    shortToggle: Boolean,
    onToggle: (Int) -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val look = checkmarkLook(value, questionMarksEnabled)
    val haptic = LocalHapticFeedback.current
    val description = stringResource(valueDescription(value))
    val toggle = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onToggle(Entry.nextToggleValue(value, skipEnabled, questionMarksEnabled))
    }
    val content = contentOf(look.tone, tones)
    // The automatic yes is the same cell with a paler accent.
    val fill = if (look.hollow) tones.accent.copy(alpha = 0.6f) else fillOf(look.tone, tones)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(DayCellHeight)
            .clip(DayCellShape)
            .background(fill)
            .semantics { contentDescription = description }
            .testTag("checkmark_$value")
            .combinedClickable(
                onClick = if (shortToggle) toggle else onEdit,
                onLongClick = if (shortToggle) onEdit else toggle
            )
            .noteDot(notes, content)
    ) {
        when (look.glyph) {
            CheckmarkGlyph.CHECK -> Icon(Icons.Filled.Check, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
            CheckmarkGlyph.TIMES -> Icon(Icons.Filled.Close, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
            else -> Text(
                glyphString(look.glyph),
                color = content,
                fontFamily = rememberFontAwesome(),
                fontSize = 16.sp
            )
        }
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
    tones: HabitTones,
    questionMarksEnabled: Boolean,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tone = numberTone(value, completed)
    val display = numberDisplay(value, questionMarksEnabled)
    val fill = fillOf(tone, tones)
    // A number that is not enough yet still reads strongly; a zero or an unknown fades.
    val content = when {
        tone == CellTone.HABIT -> tones.onAccent
        value > 0 -> tones.onContainer
        else -> tones.onSoft
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(DayCellHeight)
            .clip(DayCellShape)
            .background(fill)
            .combinedClickable(onClick = onEdit, onLongClick = onEdit)
            .noteDot(notes, content)
    ) {
        when (display) {
            NumberDisplay.Skipped -> Text(FontAwesome.SKIPPED, color = content, fontFamily = rememberFontAwesome(), fontSize = 16.sp)
            NumberDisplay.Question -> Text(FontAwesome.QUESTION, color = content, fontFamily = rememberFontAwesome(), fontSize = 14.sp)
            is NumberDisplay.Value -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 2.dp)) {
                Text(
                    display.text,
                    color = content,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                if (units.isNotBlank()) {
                    Text(
                        units,
                        color = content.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * The icon of a habit inside a ring that shows its score: the track in the soft tone and the score on top of it in the
 * accent, starting at the top, with rounded ends.
 */
@Composable
fun HabitIconRing(
    percentage: Float,
    icon: ImageVector,
    tones: HabitTones,
    modifier: Modifier = Modifier,
    diameter: Dp = 50.dp,
    thickness: Dp = 6.dp
) {
    val sweep = 360f * percentage.coerceIn(0f, 1f)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(diameter)
            .drawBehind {
                val stroke = thickness.toPx()
                val topLeft = Offset(stroke / 2, stroke / 2)
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(tones.ringTrack, -90f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
                drawArc(tones.accent, -90f, sweep, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }
    ) {
        Icon(icon, contentDescription = null, tint = tones.accent, modifier = Modifier.size(24.dp))
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
                val arcSize = Size(size.width - thickness, size.height - thickness)
                val topLeft = Offset(inset, inset)
                drawArc(inactiveColor, -90f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(thickness))
                drawArc(color, -90f, sweep, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(thickness))
            }
    )
}
