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

package io.github.crisacm.salem.activities.habits.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitErrors
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.ui.edit.frequencyText
import io.github.crisacm.salem.ui.edit.numericalFrequencyText
import io.github.crisacm.salem.ui.edit.rememberTimeFormat
import io.github.crisacm.salem.ui.edit.weekdaysText
import java.util.Locale

private const val MAX_NAME_LENGTH = 50

class EditHabitActions(
    val onBack: () -> Unit,
    val onSave: (EditHabitForm) -> Unit,
    val onPickColor: () -> Unit,
    val onPickFrequency: () -> Unit,
    val onPickTargetType: () -> Unit,
    val onPickNumericalFrequency: () -> Unit,
    val onPickTime: () -> Unit,
    val onPickDays: () -> Unit
)

/**
 * The create and edit habit screen. The `testTag`s are exposed as resource ids so that UiAutomator
 * tests can find the fields.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EditHabitContent(
    form: EditHabitForm,
    onFormChange: (EditHabitForm) -> Unit,
    isEditing: Boolean,
    habitColor: Color,
    barColor: Color,
    onBarColor: Color,
    actions: EditHabitActions
) {
    val timeFormat = rememberTimeFormat()
    val dateFormatter = remember { JavaLocalDateFormatter(Locale.getDefault()) }
    var attempted by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val errors = if (attempted) form.validate() else EditHabitErrors(nameBlank = false, targetBlank = false)
    val isNumerical = form.type == HabitType.NUMERICAL
    val blank = stringResource(R.string.validation_cannot_be_blank)

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isEditing) R.string.edit_habit else R.string.create_habit)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_up))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            attempted = true
                            if (form.validate().isValid) {
                                saving = true
                                actions.onSave(form)
                            }
                        },
                        enabled = !saving,
                        modifier = Modifier.testTag("buttonSave")
                    ) { Text(stringResource(R.string.save), color = onBarColor) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = barColor,
                    titleContentColor = onBarColor,
                    navigationIconContentColor = onBarColor
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextInput(
                label = R.string.name,
                hint = if (isNumerical) R.string.measurable_short_example else R.string.yes_or_no_short_example,
                value = form.name,
                tag = "nameInput",
                error = if (errors.nameBlank) blank else null,
                maxLines = 2,
                capitalization = KeyboardCapitalization.Sentences
            ) { onFormChange(form.copy(name = it.take(MAX_NAME_LENGTH))) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().testTag("colorButton").clickable(onClick = actions.onPickColor)
            ) {
                Text(
                    stringResource(R.string.color),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(habitColor))
            }

            TextInput(
                label = R.string.question,
                hint = if (isNumerical) R.string.measurable_question_example else R.string.example_question_boolean,
                value = form.question,
                tag = "questionInput",
                capitalization = KeyboardCapitalization.Sentences
            ) { onFormChange(form.copy(question = it)) }

            if (!isNumerical) {
                PickerRow(R.string.frequency, frequencyText(form.freqNum, form.freqDen), "frequencyPicker", actions.onPickFrequency)
            } else {
                TextInput(
                    label = R.string.unit,
                    hint = R.string.measurable_units_example,
                    value = form.unit,
                    tag = "unitInput",
                    maxLines = 1
                ) { onFormChange(form.copy(unit = it)) }
                TextInput(
                    label = R.string.target,
                    hint = R.string.example_target,
                    value = form.targetText,
                    tag = "targetInput",
                    error = if (errors.targetBlank) blank else null,
                    maxLines = 1,
                    keyboardType = KeyboardType.Decimal
                ) { onFormChange(form.copy(targetText = it)) }
                PickerRow(R.string.frequency, numericalFrequencyText(form.freqNum, form.freqDen), "numericalFrequencyPicker", actions.onPickNumericalFrequency)
                PickerRow(
                    R.string.target_type,
                    stringResource(
                        if (form.targetType == NumericalHabitType.AT_MOST) R.string.target_type_at_most else R.string.target_type_at_least
                    ),
                    "targetTypePicker",
                    actions.onPickTargetType
                )
            }

            PickerRow(
                R.string.reminder,
                if (form.hasReminder) timeFormat.format(form.reminderHour, form.reminderMin) else stringResource(R.string.reminder_off),
                "reminderTimePicker",
                actions.onPickTime
            )
            if (form.hasReminder) {
                PickerRow(null, weekdaysText(form.reminderDays, dateFormatter), "reminderDatePicker", actions.onPickDays)
            }

            TextInput(
                label = R.string.notes,
                hint = R.string.example_notes,
                value = form.notes,
                tag = "notesInput",
                capitalization = KeyboardCapitalization.Sentences
            ) { onFormChange(form.copy(notes = it)) }
        }
    }
}

@Composable
private fun TextInput(
    label: Int,
    hint: Int,
    value: String,
    tag: String,
    error: String? = null,
    maxLines: Int = Int.MAX_VALUE,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        placeholder = { Text(stringResource(hint)) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        maxLines = maxLines,
        singleLine = maxLines == 1,
        keyboardOptions = KeyboardOptions(capitalization = capitalization, keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth().testTag(tag)
    )
}

@Composable
private fun PickerRow(label: Int?, value: String, tag: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().testTag(tag).clickable(onClick = onClick)) {
        if (label != null) {
            Text(stringResource(label), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
        }
        Text(
            value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}
