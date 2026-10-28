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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitErrors
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.platform.time.getFirstWeekday
import io.github.crisacm.salem.ui.compose.toCompose
import io.github.crisacm.salem.ui.compose.toExpressiveColorScheme
import io.github.crisacm.salem.ui.compose.usesDarkTones
import io.github.crisacm.salem.ui.dialogs.ChoiceDialogContent
import io.github.crisacm.salem.ui.dialogs.ColorPickerContent
import io.github.crisacm.salem.ui.dialogs.FrequencyPickerContent
import io.github.crisacm.salem.ui.dialogs.TimePickerContent
import io.github.crisacm.salem.ui.dialogs.paletteColors
import io.github.crisacm.salem.ui.list.exposeTestTags
import io.github.crisacm.salem.ui.list.habitTones
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.cancel
import io.github.crisacm.salem.ui.resources.color
import io.github.crisacm.salem.ui.resources.color_picker_default_title
import io.github.crisacm.salem.ui.resources.create_habit
import io.github.crisacm.salem.ui.resources.custom
import io.github.crisacm.salem.ui.resources.edit_habit
import io.github.crisacm.salem.ui.resources.every_day
import io.github.crisacm.salem.ui.resources.every_month
import io.github.crisacm.salem.ui.resources.every_week
import io.github.crisacm.salem.ui.resources.frequency
import io.github.crisacm.salem.ui.resources.measurable
import io.github.crisacm.salem.ui.resources.name
import io.github.crisacm.salem.ui.resources.navigate_up
import io.github.crisacm.salem.ui.resources.notes
import io.github.crisacm.salem.ui.resources.ok
import io.github.crisacm.salem.ui.resources.per_week
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
import io.github.crisacm.salem.ui.resources.yes_no_option
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
    data object NumericalFrequency : EditPicker
    data object Time : EditPicker
}

/**
 * The create and edit habit screen. It keeps the form and opens the pickers over it; [onSave] gets a valid form and
 * the host decides when to leave. The `testTag`s are exposed as resource ids so that UiAutomator tests can find them.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditHabitScreen(
    initial: EditHabitForm,
    isEditing: Boolean,
    theme: Theme,
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
    val errors = if (attempted) form.validate() else EditHabitErrors(nameBlank = false, targetBlank = false)
    val isNumerical = form.type == HabitType.NUMERICAL
    val blank = stringResource(Res.string.validation_cannot_be_blank)
    val close = { picker = EditPicker.None }

    BackHandler(enabled = picker != EditPicker.None) { close() }

    MaterialTheme(colorScheme = theme.toExpressiveColorScheme()) {
        val scheme = MaterialTheme.colorScheme
        val dark = theme.usesDarkTones()
        val habitColor = theme.color(form.color).toCompose()
        Box(Modifier.fillMaxSize().background(scheme.background).exposeTestTags()) {
            Column(
                // The form stays behind the picker: hide it from accessibility so that its Save and fields are not found.
                modifier = (if (picker == EditPicker.None) Modifier else Modifier.clearAndSetSemantics { })
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            ) {
                EditHeader(
                    title = stringResource(if (isEditing) Res.string.edit_habit else Res.string.create_habit),
                    icon = if (isEditing) Icons.Filled.Edit else Icons.Filled.Add,
                    backDescription = stringResource(Res.string.navigate_up),
                    saveLabel = stringResource(Res.string.save),
                    saveEnabled = !saving,
                    onBack = onBack,
                    onSave = {
                        attempted = true
                        if (form.validate().isValid) {
                            saving = true
                            onSave(form)
                        }
                    }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SegmentedChoice(
                        options = listOf(stringResource(Res.string.yes_no_option), stringResource(Res.string.measurable)),
                        selected = if (isNumerical) 1 else 0,
                        onSelect = { form = form.withType(if (it == 1) HabitType.NUMERICAL else HabitType.YES_NO) },
                        tags = listOf("typeYesNo", "typeMeasurable")
                    )

                    NameTextField(
                        label = stringResource(Res.string.name),
                        value = form.name,
                        tag = "nameInput",
                        error = if (errors.nameBlank) blank else null
                    ) { form = form.copy(name = it.take(MAX_NAME_LENGTH)) }

                    FormCard {
                        val swatches = swatchColors(form.color)
                        val colorLabel = stringResource(Res.string.color)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                colorLabel,
                                color = scheme.primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            ColorSwatches(
                                swatches = swatches.map { habitTones(it, dark).accent },
                                selectedIndex = swatches.indexOf(form.color.paletteIndex),
                                checkColor = habitTones(form.color.paletteIndex, dark).onAccent,
                                moreColor = scheme.onSurfaceVariant,
                                onPick = { form = form.copy(color = PaletteColor(swatches[it])) },
                                onMore = { picker = EditPicker.Color },
                                moreDescription = stringResource(Res.string.color_picker_default_title),
                                swatchDescription = { "$colorLabel ${swatches[it] + 1}" }
                            )
                        }
                    }

                    FormTextField(
                        label = stringResource(Res.string.question),
                        value = form.question,
                        tag = "questionInput",
                        capitalization = KeyboardCapitalization.Sentences
                    ) { form = form.copy(question = it) }

                    if (isNumerical) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FormTextField(
                                label = stringResource(Res.string.unit),
                                value = form.unit,
                                tag = "unitInput",
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            ) { form = form.copy(unit = it) }
                            FormTextField(
                                label = stringResource(Res.string.target),
                                value = form.targetText,
                                tag = "targetInput",
                                modifier = Modifier.weight(1f),
                                error = if (errors.targetBlank) blank else null,
                                maxLines = 1,
                                keyboardType = KeyboardType.Decimal
                            ) { form = form.copy(targetText = it) }
                        }
                        FormCard(label = stringResource(Res.string.target_type)) {
                            SegmentedChoice(
                                options = listOf(
                                    stringResource(Res.string.target_type_at_least),
                                    stringResource(Res.string.target_type_at_most)
                                ),
                                selected = if (form.targetType == NumericalHabitType.AT_MOST) 1 else 0,
                                onSelect = {
                                    form = form.copy(targetType = if (it == 0) NumericalHabitType.AT_LEAST else NumericalHabitType.AT_MOST)
                                },
                                height = 54.dp,
                                tags = listOf("targetTypeAtLeast", "targetTypeAtMost")
                            )
                        }
                    }

                    FormCard(label = stringResource(Res.string.frequency)) {
                        val choice = frequencyChoice(form.freqNum, form.freqDen, isNumerical)
                        PillRow {
                            ChoicePill(stringResource(Res.string.every_day), choice == FrequencyChoice.EVERY_DAY, {
                                form = form.copy(freqNum = 1, freqDen = 1)
                            })
                            ChoicePill(stringResource(Res.string.per_week), choice == FrequencyChoice.PER_WEEK, {
                                form = form.copy(freqNum = 1, freqDen = 7)
                            })
                            ChoicePill(
                                stringResource(Res.string.custom),
                                choice == FrequencyChoice.CUSTOM,
                                { picker = if (isNumerical) EditPicker.NumericalFrequency else EditPicker.Frequency },
                                Modifier.testTag(if (isNumerical) "numericalFrequencyPicker" else "frequencyPicker")
                            )
                        }
                        if (choice == FrequencyChoice.CUSTOM) {
                            Text(
                                if (isNumerical) numericalFrequencyText(form.freqNum, form.freqDen) else frequencyText(form.freqNum, form.freqDen),
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }

                    FormCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().testTag("reminderTimePicker").clickable { picker = EditPicker.Time }
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(Res.string.reminder),
                                    color = scheme.primary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (form.hasReminder) timeFormat.format(form.reminderHour, form.reminderMin) else stringResource(Res.string.reminder_off),
                                    color = scheme.onSurface,
                                    fontSize = 28.sp
                                )
                            }
                            Switch(
                                checked = form.hasReminder,
                                onCheckedChange = { on -> if (on) picker = EditPicker.Time else form = form.clearReminder() }
                            )
                        }
                        if (form.hasReminder) {
                            WeekdayCircles(
                                slots = remember { weekdaySlots(getFirstWeekday()) },
                                selected = form.reminderDays.toArray(),
                                letter = { dateFormatter.narrowWeekdayName(it.day) },
                                description = { dateFormatter.longWeekdayName(it.day) },
                                onToggle = { form = form.copy(reminderDays = toggleWeekday(form.reminderDays, it.index)) },
                                modifier = Modifier.padding(top = 16.dp).testTag("reminderDatePicker")
                            )
                        }
                    }

                    FormTextField(
                        label = stringResource(Res.string.notes),
                        value = form.notes,
                        tag = "notesInput",
                        minLines = 4,
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
            }
        }
    }
}
