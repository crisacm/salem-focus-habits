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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.ui.compose.DialogButton
import io.github.crisacm.salem.ui.compose.DialogSurface
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.cancel
import io.github.crisacm.salem.ui.resources.change_type
import io.github.crisacm.salem.ui.resources.change_type_message
import io.github.crisacm.salem.ui.resources.change_type_title
import org.jetbrains.compose.resources.stringResource

/**
 * Warns that changing the type of a habit changes how it works: what was recorded is kept, but it is not shown, as
 * it is in another format. The host closes the dialog after either callback.
 */
@Composable
fun ConfirmTypeChangeDialogContent(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    DialogSurface(
        title = stringResource(Res.string.change_type_title),
        onDismiss = onDismiss,
        buttons = {
            DialogButton(stringResource(Res.string.cancel), "changeTypeCancel", onDismiss)
            DialogButton(stringResource(Res.string.change_type), "changeTypeConfirm", onConfirm)
        }
    ) {
        Text(
            stringResource(Res.string.change_type_message),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}
