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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.platform.gui.FontAwesome
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.no_habits_found
import io.github.crisacm.salem.ui.resources.no_habits_left_to_do
import org.jetbrains.compose.resources.stringResource

/** The icon and the text shown when the list has nothing to display; draws nothing when [state] is hidden. */
@Composable
fun EmptyListContent(state: EmptyListState) {
    if (state == EmptyListState.HIDDEN) return
    val glyphs = rememberFontAwesome()
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    val (icon, text) = when (state) {
        EmptyListState.DONE -> FontAwesome.UMBRELLA_BEACH to Res.string.no_habits_left_to_do
        else -> FontAwesome.STAR_HALF_O to Res.string.no_habits_found
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // The glyph is a private-use character: screen readers should only read the text below.
        Text(
            icon,
            color = color,
            fontFamily = glyphs,
            fontSize = 40.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics {}
        )
        Text(
            stringResource(text),
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}
