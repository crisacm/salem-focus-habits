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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm
import io.github.crisacm.salem.core.ui.screens.habits.edit.HabitEditorSaver
import io.github.crisacm.salem.core.ui.screens.habits.edit.SaveResult
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.compose.toCompose
import io.github.crisacm.salem.ui.dialogs.CheckmarkPopup
import io.github.crisacm.salem.ui.dialogs.ColorPickerContent
import io.github.crisacm.salem.ui.dialogs.ConfirmDeleteDialogContent
import io.github.crisacm.salem.ui.dialogs.HabitTypeDialogContent
import io.github.crisacm.salem.ui.dialogs.NumberPopup
import io.github.crisacm.salem.ui.dialogs.paletteColors
import io.github.crisacm.salem.ui.edit.EditHabitScreen
import io.github.crisacm.salem.ui.edit.PickerLabels
import io.github.crisacm.salem.ui.edit.defaultPickerLabels

/**
 * Draws the open overlay of [overlays] over the habit list, with the shared dialogs, popups and editor. Back closes
 * it; a popup closed that way still saves the notes that changed. [saverFactory] makes a new saver for each editor,
 * and [decimalSeparator] and [textKeyboard] shape the number popup.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ListOverlayHost(
    overlays: ListOverlays,
    theme: Theme,
    dateFormatter: LocalDateFormatter,
    preferences: Preferences,
    habitList: HabitList,
    saverFactory: () -> HabitEditorSaver,
    decimalSeparator: Char,
    textKeyboard: Boolean = false,
    labels: PickerLabels = defaultPickerLabels()
) {
    val overlay = overlays.current ?: return
    val dismiss = overlays::dismiss
    // A new overlay starts with fresh state, even when it is of the same kind as the one it replaces.
    key(overlay) {
        when (overlay) {
            ListOverlay.TypeChoice -> {
                BackHandler(onBack = dismiss)
                HabitTypeDialogContent(onSelected = { overlays.showEditor(null, it) }, onDismiss = dismiss)
            }
            is ListOverlay.Editor -> {
                BackHandler(onBack = dismiss)
                val habit = overlay.habitId?.let { habitList.getById(it) }
                if (overlay.habitId != null && habit == null) {
                    // The habit was deleted while the editor was about to open.
                    LaunchedEffect(Unit) { dismiss() }
                } else {
                    val saver = remember { saverFactory() }
                    EditHabitScreen(
                        initial = habit?.let { EditHabitForm.from(it) } ?: EditHabitForm.new(overlay.type),
                        isEditing = habit != null,
                        theme = theme,
                        isNight = theme is DarkTheme,
                        dateFormatter = dateFormatter,
                        onSave = { form -> if (saver.save(form, overlay.habitId) != SaveResult.ALREADY_SAVING) dismiss() },
                        onBack = dismiss,
                        labels = labels
                    )
                }
            }
            is ListOverlay.Delete -> {
                BackHandler(onBack = dismiss)
                ConfirmDeleteDialogContent(
                    quantity = overlay.quantity,
                    onConfirm = {
                        dismiss()
                        overlay.callback.onConfirmed()
                    },
                    onDismiss = dismiss
                )
            }
            is ListOverlay.Color -> {
                BackHandler(onBack = dismiss)
                ColorPickerContent(
                    palette = remember(theme) { theme.paletteColors() },
                    selected = overlay.selected.paletteIndex,
                    onPicked = {
                        dismiss()
                        overlay.callback.onColorPicked(PaletteColor(it))
                    },
                    onDismiss = dismiss
                )
            }
            is ListOverlay.Checkmark -> {
                val close = {
                    dismiss()
                    overlay.session.pendingNotesOnDismiss()?.let { overlay.callback.onNotesSaved(overlay.value, it) }
                    Unit
                }
                BackHandler(onBack = close)
                CheckmarkPopup(
                    habitColor = theme.color(overlay.color).toCompose(),
                    session = overlay.session,
                    originalValue = overlay.value,
                    skipEnabled = preferences.isSkipEnabled,
                    unknownEnabled = preferences.areQuestionMarksEnabled,
                    onChoose = { value, notes ->
                        dismiss()
                        overlay.callback.onNotesSaved(value, notes)
                    },
                    onDismiss = close
                )
            }
            is ListOverlay.Number -> {
                val close = {
                    dismiss()
                    overlay.session.pendingNotesOnDismiss()?.let { overlay.callback.onNumberPicked(overlay.value, it) }
                    Unit
                }
                BackHandler(onBack = close)
                NumberPopup(
                    session = overlay.session,
                    originalValue = overlay.value,
                    skipEnabled = preferences.isSkipEnabled,
                    unknownEnabled = preferences.areQuestionMarksEnabled,
                    decimalSeparator = decimalSeparator,
                    textKeyboard = textKeyboard,
                    onSave = { value, notes ->
                        dismiss()
                        overlay.callback.onNumberPicked(value, notes)
                    },
                    onDismiss = close
                )
            }
        }
    }
}
