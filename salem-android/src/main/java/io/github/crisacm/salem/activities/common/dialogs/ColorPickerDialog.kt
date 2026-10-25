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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.callbacks.OnColorPickedCallback
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.ui.dialogs.ColorPickerContent
import io.github.crisacm.salem.utils.StyledResources

/**
 * Dialog that allows the user to choose a color.
 */
class ColorPickerDialog : ComposeDialogFragment() {
    private var callback: OnColorPickedCallback? = null

    fun setListener(callback: OnColorPickedCallback) {
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The listener is not saved: a dialog restored after a rotation could not report anything.
        if (savedInstanceState != null) dismissAllowingStateLoss()
    }

    /** Sets the colour that is shown as selected; it survives the recreation of the fragment. */
    fun initialize(selected: PaletteColor) {
        arguments = Bundle().apply { putInt(KEY_SELECTED, selected.paletteIndex) }
    }

    @Composable
    override fun Content() {
        val palette = remember { StyledResources(requireContext()).getPalette().map { Color(it) } }
        ColorPickerContent(
            palette = palette,
            selected = arguments?.getInt(KEY_SELECTED, -1) ?: -1,
            onPicked = {
                callback?.onColorPicked(PaletteColor(it))
                dismiss()
            },
            onDismiss = ::dismiss
        )
    }

    companion object {
        private const val KEY_SELECTED = "selectedColor"
    }
}
