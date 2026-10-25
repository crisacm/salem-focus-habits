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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.ui.compose.DialogButton
import io.github.crisacm.salem.ui.compose.DialogSurface
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.cancel
import io.github.crisacm.salem.ui.resources.ok
import io.github.crisacm.salem.ui.resources.select_weekdays
import org.jetbrains.compose.resources.stringResource

/**
 * Checkboxes for the days of the week, named by [dayNames] (Saturday first, as a [WeekdayList]); [onPicked] gets
 * the checked days. The host closes the dialog.
 */
@Composable
fun WeekdayPickerContent(
    dayNames: List<String>,
    selected: WeekdayList,
    onPicked: (WeekdayList) -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = stringResource(Res.string.ok),
    cancelLabel: String = stringResource(Res.string.cancel)
) {
    val checked = remember { mutableStateListOf(*selected.toArray().toTypedArray()) }
    DialogSurface(
        title = stringResource(Res.string.select_weekdays),
        onDismiss = onDismiss,
        buttons = {
            DialogButton(cancelLabel, onDismiss)
            DialogButton(confirmLabel) { onPicked(WeekdayList(checked.toBooleanArray())) }
        }
    ) {
        dayNames.forEachIndexed { index, name ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Checkbox) { checked[index] = !checked[index] }
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            ) {
                Checkbox(checked = checked[index], onCheckedChange = null)
                Text(name, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}
