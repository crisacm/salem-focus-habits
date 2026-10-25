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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.ui.screens.habits.list.HintList
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.hint_title
import org.jetbrains.compose.resources.stringResource

private const val FADE_MILLIS = 500

private val HintBackground = Color(0xFF3F51B5)

/** The "Did you know?" banner: it asks [hintList] for a hint once, fades in, and fades out when tapped. */
@Composable
fun HintContent(hintList: HintList) {
    var hint by remember { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hint = nextHint(hintList)
        visible = hint != null
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(FADE_MILLIS)),
        exit = fadeOut(tween(FADE_MILLIS))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HintBackground)
                .clickable { visible = false }
                .padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 16.dp)
        ) {
            Text(stringResource(Res.string.hint_title), color = Color.White, fontWeight = FontWeight.Bold)
            Text(hint.orEmpty(), color = Color.White, modifier = Modifier.padding(top = 5.dp))
        }
    }
}
