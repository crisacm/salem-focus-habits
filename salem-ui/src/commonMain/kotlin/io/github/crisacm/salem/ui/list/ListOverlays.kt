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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.callbacks.CheckMarkDialogCallback
import io.github.crisacm.salem.core.ui.callbacks.NumberPickerCallback
import io.github.crisacm.salem.core.ui.callbacks.OnColorPickedCallback
import io.github.crisacm.salem.core.ui.callbacks.OnConfirmedCallback
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.dialogs.NotesSession

/**
 * What is shown over the habit list: the dialogs, the popups and the editor that the list asks for. Each one is its
 * own object (not a value), so showing the same kind twice starts with fresh state.
 */
sealed interface ListOverlay {
    /** The choice between a yes or no habit and a numerical one. */
    data object TypeChoice : ListOverlay

    /** The editor: a new habit of [type], or the habit with [habitId]. */
    class Editor(val habitId: Long?, val type: HabitType) : ListOverlay

    class Delete(val quantity: Int, val callback: OnConfirmedCallback) : ListOverlay

    class Color(val selected: PaletteColor, val callback: OnColorPickedCallback) : ListOverlay

    /** The popup of a yes or no cell; [session] keeps the notes while it is open. */
    class Checkmark(
        val value: Int,
        notes: String,
        val color: PaletteColor,
        val callback: CheckMarkDialogCallback
    ) : ListOverlay {
        val session = NotesSession(notes)
    }

    /** The popup of a numerical cell; [session] keeps the notes while it is open. */
    class Number(val habit: Habit, val date: LocalDate, val value: Double, notes: String, val callback: NumberPickerCallback) : ListOverlay {
        val session = NotesSession(notes)
    }
}

/** The overlay of the habit list that is open, if any: the screen of the platform fills it in, [ListOverlayHost] draws it. */
class ListOverlays {
    var current by mutableStateOf<ListOverlay?>(null)
        private set

    fun showHabitType() = show(ListOverlay.TypeChoice)

    fun showEditor(habitId: Long?, type: HabitType) = show(ListOverlay.Editor(habitId, type))

    fun showDelete(quantity: Int, callback: OnConfirmedCallback) = show(ListOverlay.Delete(quantity, callback))

    fun showColor(selected: PaletteColor, callback: OnColorPickedCallback) = show(ListOverlay.Color(selected, callback))

    fun showCheckmark(value: Int, notes: String, color: PaletteColor, callback: CheckMarkDialogCallback) =
        show(ListOverlay.Checkmark(value, notes, color, callback))

    fun showNumber(habit: Habit, date: LocalDate, value: Double, notes: String, callback: NumberPickerCallback) =
        show(ListOverlay.Number(habit, date, value, notes, callback))

    /** Closes [overlay] if it is still the open one: a late callback of an older overlay must not close a newer one. */
    fun dismiss(overlay: ListOverlay) {
        if (current === overlay) current = null
    }

    private fun show(overlay: ListOverlay) {
        current = overlay
    }
}

/** Hides what is under an open overlay from accessibility services, which share its window. */
fun Modifier.hiddenWhileOverlay(overlays: ListOverlays): Modifier =
    if (overlays.current != null) clearAndSetSemantics { } else this
