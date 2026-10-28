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

package io.github.crisacm.salem.ui.edit

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CardShape = RoundedCornerShape(28.dp)
private val FieldShape = RoundedCornerShape(28.dp)

@Composable
internal fun isDarkScheme() = MaterialTheme.colorScheme.background.luminance() < 0.5f

/**
 * The top of the editor: a rounded panel under the status bar with the back and save buttons, and below them the
 * badge and the large title.
 */
@Composable
internal fun EditHeader(
    title: String,
    icon: ImageVector,
    backDescription: String,
    saveLabel: String,
    saveEnabled: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.primaryContainer,
        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isDarkScheme()) Color.White.copy(alpha = 0.15f) else scheme.surface.copy(alpha = 0.8f))
                        .clickable(onClick = onBack)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = backDescription,
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Button(
                    onClick = onSave,
                    enabled = saveEnabled,
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary, contentColor = scheme.onPrimary),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("buttonSave")
                ) {
                    Text(saveLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(72.dp).clip(ScallopShape()).background(scheme.primary)
                ) {
                    Icon(icon, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(36.dp))
                }
                Text(
                    title,
                    color = scheme.onPrimaryContainer,
                    fontSize = 40.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Two options side by side: the selected one is a pill filled with the primary colour and a check, the other a rounded rectangle. */
@Composable
internal fun SegmentedChoice(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    tags: List<String> = options.map { "" }
) {
    val scheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            val corner by animateDpAsState(if (isSelected) height / 2 else 20.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .clip(RoundedCornerShape(corner))
                    .background(if (isSelected) scheme.primary else scheme.surfaceContainerHighest)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(index) })
                    .then(if (tags[index].isNotEmpty()) Modifier.testTag(tags[index]) else Modifier)
            ) {
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(20.dp))
                }
                Text(
                    label,
                    color = if (isSelected) scheme.onPrimary else scheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = if (isSelected) 8.dp else 0.dp)
                )
            }
        }
    }
}

/** A rounded panel of the form, with an optional [label] in the primary colour on top. */
@Composable
internal fun FormCard(
    modifier: Modifier = Modifier,
    label: String? = null,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(color = scheme.surfaceContainer, shape = CardShape, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            if (label != null) {
                Text(label, color = scheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            }
            content()
        }
    }
}

/** A text field of the form: a rounded panel with the label as its hint. */
@Composable
internal fun FormTextField(
    label: String,
    value: String,
    tag: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    TextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        minLines = minLines,
        maxLines = maxLines,
        singleLine = maxLines == 1,
        shape = FieldShape,
        keyboardOptions = KeyboardOptions(capitalization = capitalization, keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = scheme.surfaceContainer,
            unfocusedContainerColor = scheme.surfaceContainer,
            errorContainerColor = scheme.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedPlaceholderColor = scheme.onSurfaceVariant,
            unfocusedPlaceholderColor = scheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp).testTag(tag)
    )
}

/** The name of the habit: the same panel as the other fields with the primary outline of the field that has the focus. */
@Composable
internal fun NameTextField(
    label: String,
    value: String,
    tag: String,
    error: String?,
    onChange: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        maxLines = 2,
        shape = FieldShape,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = scheme.surfaceContainer,
            unfocusedContainerColor = scheme.surfaceContainer,
            errorContainerColor = scheme.surfaceContainer,
            focusedBorderColor = scheme.primary,
            unfocusedBorderColor = scheme.outline.copy(alpha = 0.4f),
            focusedPlaceholderColor = scheme.onSurfaceVariant,
            unfocusedPlaceholderColor = scheme.onSurfaceVariant
        ),
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag(tag)
    )
}

/** An option of a group that wraps: a pill with an outline, or filled with a check when it is the [selected] one. */
@Composable
internal fun ChoicePill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(if (selected) scheme.primaryContainer else Color.Transparent)
            .then(if (selected) Modifier else Modifier.border(BorderStroke(1.dp, scheme.outline), CircleShape))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 20.dp)
    ) {
        val content = if (selected) scheme.onPrimaryContainer else scheme.onSurface
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        }
        Text(
            label,
            color = content,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = if (selected) 8.dp else 0.dp)
        )
    }
}

/** The chips of a frequency; they wrap onto another line when they do not fit. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PillRow(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) { content() }
}

/** The seven days of the week as round toggles; [slots] are in the order they are shown. */
@Composable
internal fun WeekdayCircles(
    slots: List<WeekdaySlot>,
    selected: BooleanArray,
    letter: (WeekdaySlot) -> String,
    description: (WeekdaySlot) -> String,
    onToggle: (WeekdaySlot) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier.fillMaxWidth()) {
        slots.forEach { slot ->
            val on = selected[slot.index]
            val name = description(slot)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(if (on) scheme.primary else scheme.surfaceContainerHighest)
                    .semantics { contentDescription = name }
                    .selectable(selected = on, role = Role.Checkbox, onClick = { onToggle(slot) })
            ) {
                Text(
                    letter(slot),
                    color = if (on) scheme.onPrimary else scheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * The colour of the habit as a row of swatches; the selected one is a scalloped badge with a check, and the last
 * one opens the whole palette. [swatches] are the colours to paint and [selectedIndex] is the position of the one of the habit.
 */
@Composable
internal fun ColorSwatches(
    swatches: List<Color>,
    selectedIndex: Int,
    checkColor: Color,
    moreColor: Color,
    onPick: (Int) -> Unit,
    onMore: () -> Unit,
    moreDescription: String,
    swatchDescription: (Int) -> String
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        swatches.forEachIndexed { index, color ->
            val isSelected = index == selectedIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onPick(index) })
                    .semantics { contentDescription = swatchDescription(index) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (isSelected) 40.dp else 32.dp)
                        .clip(if (isSelected) ScallopShape() else CircleShape)
                        .background(color)
                ) {
                    if (isSelected) Icon(Icons.Filled.Check, contentDescription = null, tint = checkColor, modifier = Modifier.size(24.dp))
                }
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onMore)
                .semantics { contentDescription = moreDescription }
                .testTag("colorButton")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(1.dp, moreColor), CircleShape)
            ) {
                Text("+", color = moreColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
