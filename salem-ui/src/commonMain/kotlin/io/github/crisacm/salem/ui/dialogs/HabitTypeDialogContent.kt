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

package io.github.crisacm.salem.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.ui.edit.ScallopShape
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.habit_type
import io.github.crisacm.salem.ui.resources.habit_type_subtitle
import io.github.crisacm.salem.ui.resources.measurable
import io.github.crisacm.salem.ui.resources.measurable_example
import io.github.crisacm.salem.ui.resources.yes_or_no
import io.github.crisacm.salem.ui.resources.yes_or_no_example
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The two kinds of habit to create, in a bottom sheet; [onSelected] gets the chosen one. The host closes the sheet. */
@Composable
fun HabitTypeDialogContent(onSelected: (HabitType) -> Unit, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    SalemBottomSheet(onDismiss) {
        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f).padding(top = 2.dp)) {
                Text(
                    stringResource(Res.string.habit_type),
                    color = scheme.onSurface,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(stringResource(Res.string.habit_type_subtitle), color = scheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
            }
            SheetCloseButton(onDismiss)
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TypeCard(Res.string.yes_or_no, Res.string.yes_or_no_example, "typeYesNo", { CheckGlyph() }) { onSelected(HabitType.YES_NO) }
            TypeCard(Res.string.measurable, Res.string.measurable_example, "typeMeasurable", { BarsGlyph() }) { onSelected(HabitType.NUMERICAL) }
        }
    }
}

@Composable
private fun TypeCard(
    title: StringResource,
    example: StringResource,
    tag: String,
    glyph: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(sheetCardColor())
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(56.dp).clip(ScallopShape()).background(scheme.secondaryContainer), contentAlignment = Alignment.Center) { glyph() }
        Column(Modifier.weight(1f).padding(start = 17.dp, end = 4.dp)) {
            Text(stringResource(title), color = scheme.onSurface, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(example), color = scheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun CheckGlyph() {
    Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(30.dp))
}

/** Three rounded bars of different height, the glyph of a measurable habit. */
@Composable
private fun BarsGlyph() {
    val color: Color = MaterialTheme.colorScheme.onSecondaryContainer
    Canvas(Modifier.size(28.dp)) {
        val width = 4.5.dp.toPx()
        val gap = 5.dp.toPx()
        val heights = listOf(11.dp.toPx(), 22.dp.toPx(), 16.dp.toPx())
        val left = (size.width - (3 * width + 2 * gap)) / 2
        heights.forEachIndexed { index, height ->
            drawRoundRect(
                color,
                topLeft = Offset(left + index * (width + gap), (size.height - height) / 2),
                size = Size(width, height),
                cornerRadius = CornerRadius(width / 2)
            )
        }
    }
}
