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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitErrors
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.compose.toCompose
import io.github.crisacm.salem.ui.dialogs.ChoiceDialogContent
import io.github.crisacm.salem.ui.dialogs.ColorPickerContent
import io.github.crisacm.salem.ui.dialogs.FrequencyPickerContent
import io.github.crisacm.salem.ui.dialogs.TimePickerContent
import io.github.crisacm.salem.ui.dialogs.WeekdayPickerContent
import io.github.crisacm.salem.ui.dialogs.paletteColors
import io.github.crisacm.salem.ui.dialogs.weekdayNames
import io.github.crisacm.salem.ui.list.exposeTestTags
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.cancel
import io.github.crisacm.salem.ui.resources.color
import io.github.crisacm.salem.ui.resources.create_habit
import io.github.crisacm.salem.ui.resources.edit_habit
import io.github.crisacm.salem.ui.resources.every_day
import io.github.crisacm.salem.ui.resources.every_month
import io.github.crisacm.salem.ui.resources.every_week
import io.github.crisacm.salem.ui.resources.example_notes
import io.github.crisacm.salem.ui.resources.example_question_boolean
import io.github.crisacm.salem.ui.resources.example_target
import io.github.crisacm.salem.ui.resources.frequency
import io.github.crisacm.salem.ui.resources.measurable_question_example
import io.github.crisacm.salem.ui.resources.measurable_short_example
import io.github.crisacm.salem.ui.resources.measurable_units_example
import io.github.crisacm.salem.ui.resources.name
import io.github.crisacm.salem.ui.resources.navigate_up
import io.github.crisacm.salem.ui.resources.notes
import io.github.crisacm.salem.ui.resources.ok
import io.github.crisacm.salem.ui.resources.question
import io.github.crisacm.salem.ui.resources.reminder
import io.github.crisacm.salem.ui.resources.reminder_off
import io.github.crisacm.salem.ui.resources.save
import io.github.crisacm.salem.ui.resources.target
import io.github.crisacm.salem.ui.resources.target_type
import io.github.crisacm.salem.ui.resources.target_type_at_least
import io.github.crisacm.salem.ui.resources.target_type_at_most
import io.github.crisacm.salem.ui.resources.unit
import io.github.crisacm.salem.ui.resources.validation_cannot_be_blank
import io.github.crisacm.salem.ui.resources.yes
import io.github.crisacm.salem.ui.resources.yes_or_no_short_example
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val MAX_NAME_LENGTH = 50

/** The labels of the buttons of the pickers; a platform can pass its own, already localized, ones. */
class PickerLabels(val ok: String, val cancel: String, val yes: String)

@Composable
fun defaultPickerLabels() = PickerLabels(
    ok = stringResource(Res.string.ok),
    cancel = stringResource(Res.string.cancel),
    yes = stringResource(Res.string.yes)
)

/** The picker that is open over the form, if any. */
private sealed interface EditPicker {
    data object None : EditPicker
    data object Color : EditPicker
    data object Frequency : EditPicker
    data object TargetType : EditPicker
    data object NumericalFrequency : EditPicker
    data object Time : EditPicker
    data object Days : EditPicker
}

/**
 * The create and edit habit screen. It keeps the form and opens the pickers over it; [onSave] gets a valid form and
 * the host decides when to leave. The `testTag`s are exposed as resource ids so that UiAutomator tests can find them.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EditHabitScreen(
    initial: EditHabitForm,
    isEditing: Boolean,
    theme: Theme,
    isNight: Boolean,
    dateFormatter: LocalDateFormatter,
    onSave: (EditHabitForm) -> Unit,
    onBack: () -> Unit,
    labels: PickerLabels = defaultPickerLabels()
) {
    val formState = rememberSaveable(stateSaver = EditHabitFormSaver) { mutableStateOf(initial) }
    var form by formState
    var picker by remember { mutableStateOf<EditPicker>(EditPicker.None) }
    var attempted by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val timeFormat = rememberTimeFormat()
    val habitColor = theme.color(form.color).toCompose()
    val barColor = if (isNight) MaterialTheme.colorScheme.surface else habitColor
    val onBarColor = if (isNight) MaterialTheme.colorScheme.onSurface else Color.White
    val errors = if (attempted) form.validate() else EditHabitErrors(nameBlank = false, targetBlank = false)
    val isNumerical = form.type == HabitType.NUMERICAL
    val blank = stringResource(Res.string.validation_cannot_be_blank)
    val close = { picker = EditPicker.None }

    BackHandler(enabled = picker != EditPicker.None) { close() }

    Box(Modifier.fillMaxSize().exposeTestTags()) {
        Scaffold(
            // The form stays behind the picker: hide it from accessibility so that its Save and fields are not found.
            modifier = if (picker == EditPicker.None) Modifier else Modifier.clearAndSetSemantics { },
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(if (isEditing) Res.string.edit_habit else Res.string.create_habit)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.navigate_up))
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                attempted = true
                                if (form.validate().isValid) {
                                    saving = true
                                    onSave(form)
                                }
                            },
                            enabled = !saving,
                            modifier = Modifier.testTag("buttonSave")
                        ) { Text(stringResource(Res.string.save), color = onBarColor) }
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
                    label = Res.string.name,
                    hint = if (isNumerical) Res.string.measurable_short_example else Res.string.yes_or_no_short_example,
                    value = form.name,
                    tag = "nameInput",
                    error = if (errors.nameBlank) blank else null,
                    maxLines = 2,
                    capitalization = KeyboardCapitalization.Sentences
                ) { form = form.copy(name = it.take(MAX_NAME_LENGTH)) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().testTag("colorButton").clickable { picker = EditPicker.Color }
                ) {
                    Text(
                        stringResource(Res.string.color),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(habitColor))
                }

                TextInput(
                    label = Res.string.question,
                    hint = if (isNumerical) Res.string.measurable_question_example else Res.string.example_question_boolean,
                    value = form.question,
                    tag = "questionInput",
                    capitalization = KeyboardCapitalization.Sentences
                ) { form = form.copy(question = it) }

                if (!isNumerical) {
                    PickerRow(Res.string.frequency, frequencyText(form.freqNum, form.freqDen), "frequencyPicker") {
                        picker = EditPicker.Frequency
                    }
                } else {
                    TextInput(
                        label = Res.string.unit,
                        hint = Res.string.measurable_units_example,
                        value = form.unit,
                        tag = "unitInput",
                        maxLines = 1
                    ) { form = form.copy(unit = it) }
                    TextInput(
                        label = Res.string.target,
                        hint = Res.string.example_target,
                        value = form.targetText,
                        tag = "targetInput",
                        error = if (errors.targetBlank) blank else null,
                        maxLines = 1,
                        keyboardType = KeyboardType.Decimal
                    ) { form = form.copy(targetText = it) }
                    PickerRow(Res.string.frequency, numericalFrequencyText(form.freqNum, form.freqDen), "numericalFrequencyPicker") {
                        picker = EditPicker.NumericalFrequency
                    }
                    PickerRow(
                        Res.string.target_type,
                        stringResource(
                            if (form.targetType == NumericalHabitType.AT_MOST) Res.string.target_type_at_most else Res.string.target_type_at_least
                        ),
                        "targetTypePicker"
                    ) { picker = EditPicker.TargetType }
                }

                PickerRow(
                    Res.string.reminder,
                    if (form.hasReminder) timeFormat.format(form.reminderHour, form.reminderMin) else stringResource(Res.string.reminder_off),
                    "reminderTimePicker"
                ) { picker = EditPicker.Time }
                if (form.hasReminder) {
                    PickerRow(null, weekdaysText(form.reminderDays, dateFormatter), "reminderDatePicker") { picker = EditPicker.Days }
                }

                TextInput(
                    label = Res.string.notes,
                    hint = Res.string.example_notes,
                    value = form.notes,
                    tag = "notesInput",
                    capitalization = KeyboardCapitalization.Sentences
                ) { form = form.copy(notes = it) }
            }
        }

        when (picker) {
            EditPicker.None -> {}
            EditPicker.Color -> ColorPickerContent(
                palette = remember(theme) { theme.paletteColors() },
                selected = form.color.paletteIndex,
                onPicked = {
                    form = form.copy(color = PaletteColor(it))
                    close()
                },
                onDismiss = close
            )
            EditPicker.Frequency -> FrequencyPickerContent(
                numerator = form.freqNum,
                denominator = form.freqDen,
                onPicked = { num, den ->
                    form = form.copy(freqNum = num, freqDen = den)
                    close()
                },
                onDismiss = close
            )
            EditPicker.TargetType -> ChoiceDialogContent(
                options = listOf(stringResource(Res.string.target_type_at_least), stringResource(Res.string.target_type_at_most)),
                onSelected = {
                    form = form.copy(targetType = if (it == 0) NumericalHabitType.AT_LEAST else NumericalHabitType.AT_MOST)
                    close()
                },
                onDismiss = close
            )
            EditPicker.NumericalFrequency -> ChoiceDialogContent(
                options = listOf(
                    stringResource(Res.string.every_day),
                    stringResource(Res.string.every_week),
                    stringResource(Res.string.every_month)
                ),
                onSelected = {
                    form = form.copy(freqDen = denominatorForChoice(it))
                    close()
                },
                onDismiss = close
            )
            EditPicker.Time -> {
                val (hour, minute) = reminderStartTime(form.reminderHour, form.reminderMin)
                TimePickerContent(
                    hour = hour,
                    minute = minute,
                    is24Hour = timeFormat.is24Hour,
                    color = habitColor,
                    showClear = true,
                    onPicked = { pickedHour, pickedMinute ->
                        form = form.copy(reminderHour = pickedHour, reminderMin = pickedMinute)
                        close()
                    },
                    onCleared = {
                        form = form.clearReminder()
                        close()
                    },
                    onDismiss = close,
                    confirmLabel = labels.ok,
                    cancelLabel = labels.cancel
                )
            }
            EditPicker.Days -> WeekdayPickerContent(
                dayNames = remember(dateFormatter) { weekdayNames(dateFormatter) },
                selected = form.reminderDays,
                onPicked = { days ->
                    form = form.copy(reminderDays = if (days.isEmpty) WeekdayList.EVERY_DAY else days)
                    close()
                },
                onDismiss = close,
                confirmLabel = labels.yes,
                cancelLabel = labels.cancel
            )
        }
    }
}

@Composable
private fun TextInput(
    label: StringResource,
    hint: StringResource,
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
private fun PickerRow(label: StringResource?, value: String, tag: String, onClick: () -> Unit) {
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
