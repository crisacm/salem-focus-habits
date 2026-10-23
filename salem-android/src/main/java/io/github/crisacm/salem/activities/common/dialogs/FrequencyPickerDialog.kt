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

package io.github.crisacm.salem.activities.common.dialogs

import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.R
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.ui.compose.DialogButton
import io.github.crisacm.salem.ui.compose.DialogSurface

private class NumberField(val value: String, val maxLength: Int, val onChange: (String) -> Unit)

/** Dialog that lets the user pick how often a habit should be done; the result is a numerator and a denominator. */
class FrequencyPickerDialog(
    var freqNumerator: Int,
    var freqDenominator: Int
) : ComposeDialogFragment() {
    var onFrequencyPicked: (num: Int, den: Int) -> Unit = { _, _ -> }

    constructor() : this(1, 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The callback is not saved: a dialog restored after a rotation could not report anything.
        if (savedInstanceState != null) dismissAllowingStateLoss()
    }

    @Composable
    override fun Content() {
        var input by remember { mutableStateOf(initialFrequencyInput(freqNumerator, freqDenominator)) }
        DialogSurface(
            title = null,
            onDismiss = ::dismiss,
            buttons = {
                DialogButton(stringResource(R.string.save)) {
                    val (num, den) = frequencyFromInput(input)
                    onFrequencyPicked(num, den)
                    dismiss()
                }
            }
        ) {
            ModeRow(input.mode == FrequencyMode.EVERY_DAY, { input = input.copy(mode = FrequencyMode.EVERY_DAY) }) {
                Text(stringResource(R.string.every_day), color = MaterialTheme.colorScheme.onSurface)
            }
            NumberRow(
                selected = input.mode == FrequencyMode.EVERY_X_DAYS,
                onSelect = { input = input.copy(mode = FrequencyMode.EVERY_X_DAYS) },
                format = stringResource(R.string.every_x_days),
                fields = listOf(NumberField(input.everyXDays, 3) { input = input.copy(everyXDays = it) })
            )
            NumberRow(
                selected = input.mode == FrequencyMode.X_TIMES_PER_WEEK,
                onSelect = { input = input.copy(mode = FrequencyMode.X_TIMES_PER_WEEK) },
                format = stringResource(R.string.x_times_per_week),
                fields = listOf(NumberField(input.perWeek, 1) { input = input.copy(perWeek = it) })
            )
            NumberRow(
                selected = input.mode == FrequencyMode.X_TIMES_PER_MONTH,
                onSelect = { input = input.copy(mode = FrequencyMode.X_TIMES_PER_MONTH) },
                format = stringResource(R.string.x_times_per_month),
                fields = listOf(NumberField(input.perMonth, 2) { input = input.copy(perMonth = it) })
            )
            NumberRow(
                selected = input.mode == FrequencyMode.X_TIMES_IN_Y_DAYS,
                onSelect = { input = input.copy(mode = FrequencyMode.X_TIMES_IN_Y_DAYS) },
                format = stringResource(R.string.x_times_per_y_days),
                fields = listOf(
                    NumberField(input.x, 3) { input = input.copy(x = it) },
                    NumberField(input.y, 3) { input = input.copy(y = it) }
                )
            )
        }
    }
}

@Composable
private fun ModeRow(selected: Boolean, onSelect: () -> Unit, content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = 16.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        // The text and the fields wrap onto more lines with large fonts or long translations.
        FlowRow(
            modifier = Modifier.padding(start = 8.dp),
            verticalArrangement = Arrangement.Center,
            itemVerticalAlignment = Alignment.CenterVertically
        ) { content() }
    }
}

/** A radio with a text such as "%d times per week" whose numbers are editable fields. */
@Composable
private fun NumberRow(selected: Boolean, onSelect: () -> Unit, format: String, fields: List<NumberField>) {
    val first = remember { FocusRequester() }
    val parts = format.split("%d")
    ModeRow(selected, { onSelect().also { runCatching { first.requestFocus() } } }) {
        parts.forEachIndexed { index, part ->
            if (part.isNotBlank()) {
                Text(part.trim(), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 4.dp))
            }
            fields.getOrNull(index)?.let { field ->
                NumberBox(field, onFocus = onSelect, modifier = if (index == 0) Modifier.focusRequester(first) else Modifier)
            }
        }
    }
}

@Composable
private fun NumberBox(field: NumberField, onFocus: () -> Unit, modifier: Modifier) {
    BasicTextField(
        value = field.value,
        onValueChange = { if (it.length <= field.maxLength && it.all(Char::isDigit)) field.onChange(it) },
        singleLine = true,
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, textAlign = TextAlign.Center),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
            .size(width = 50.dp, height = 40.dp)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .onFocusChanged { if (it.isFocused) onFocus() }
    )
}
