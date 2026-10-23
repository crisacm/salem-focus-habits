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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.callbacks.OnColorPickedCallback
import io.github.crisacm.salem.ui.compose.ComposeDialogFragment
import io.github.crisacm.salem.ui.compose.DialogSurface
import io.github.crisacm.salem.utils.StyledResources

private const val COLUMNS = 4

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
        val palette = remember { StyledResources(requireContext()).getPalette() }
        val selected = arguments?.getInt(KEY_SELECTED, -1) ?: -1
        DialogSurface(title = stringResource(R.string.color_picker_default_title), onDismiss = ::dismiss) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                paletteRows(palette.size, COLUMNS).forEach { row ->
                    Row {
                        row.forEach { swatch ->
                            val description = stringResource(
                                if (swatch.index == selected) R.string.color_swatch_description_selected else R.string.color_swatch_description,
                                swatch.number
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(palette[swatch.index]))
                                    .semantics {
                                        contentDescription = description
                                        role = Role.Button
                                    }
                                    .clickable {
                                        callback?.onColorPicked(PaletteColor(swatch.index))
                                        dismiss()
                                    }
                            ) {
                                if (swatch.index == selected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val KEY_SELECTED = "selectedColor"
    }
}
