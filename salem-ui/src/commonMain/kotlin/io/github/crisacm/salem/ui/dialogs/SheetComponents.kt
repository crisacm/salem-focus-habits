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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.ui.edit.isDarkScheme
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.cancel
import org.jetbrains.compose.resources.stringResource

/** The colour of a bottom sheet, as drawn in the design (the pure black scheme keeps its own container). */
@Composable
internal fun sheetColor(): Color {
    val scheme = MaterialTheme.colorScheme
    return when {
        !isDarkScheme() -> Color(0xFFFBF8FD)
        scheme.background == Color.Black -> scheme.surfaceContainer
        else -> Color(0xFF1B1B21)
    }
}

/** The fill of the cards and the fields inside a sheet. */
@Composable
internal fun sheetCardColor(): Color = MaterialTheme.colorScheme.surfaceContainerHigh

/**
 * A modal bottom sheet in the style of the design: rounded top corners of 28 dp, a short drag handle and no
 * partly open state. [onDismiss] is called when the sheet is swiped down or the scrim is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SalemBottomSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = sheetColor(),
        dragHandle = { SheetHandle() },
        content = content
    )
}

@Composable
private fun SheetHandle() {
    Box(Modifier.padding(vertical = 14.dp).size(width = 32.dp, height = 4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
}

/** The round button that closes a sheet, at its top right. */
@Composable
internal fun SheetCloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(Res.string.cancel)
    Box(
        modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(closeButtonColor())
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = label
                role = Role.Button
            }
            .testTag("sheetClose"),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
    }
}
