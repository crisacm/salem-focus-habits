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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.ui.compose.DialogSurface
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.color_picker_default_title
import io.github.crisacm.salem.ui.resources.color_swatch_description
import io.github.crisacm.salem.ui.resources.color_swatch_description_selected
import org.jetbrains.compose.resources.stringResource

private const val COLUMNS = 4

/** A grid of the colours of [palette]; [onPicked] gets the palette index of the tapped one. The host closes the dialog. */
@Composable
fun ColorPickerContent(palette: List<Color>, selected: Int, onPicked: (Int) -> Unit, onDismiss: () -> Unit) {
    DialogSurface(title = stringResource(Res.string.color_picker_default_title), onDismiss = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            paletteRows(palette.size, COLUMNS).forEach { row ->
                Row {
                    row.forEach { swatch ->
                        val description = stringResource(
                            if (swatch.index == selected) Res.string.color_swatch_description_selected else Res.string.color_swatch_description,
                            swatch.number
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(palette[swatch.index])
                                .semantics {
                                    contentDescription = description
                                    role = Role.Button
                                }
                                .clickable { onPicked(swatch.index) }
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
