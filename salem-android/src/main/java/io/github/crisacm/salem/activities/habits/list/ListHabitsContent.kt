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

package io.github.crisacm.salem.activities.habits.list

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.crisacm.salem.ui.compose.resolveTheme
import io.github.crisacm.salem.ui.compose.systemIsDark

/**
 * The whole habit list screen in one composition: the top bar, the day header, the rows (or the empty
 * state), the task progress bar and the hint. The confetti is a View laid over this one by the activity.
 */
@Composable
fun ListHabitsContent(host: ListHabitsHost) {
    DisposableEffect(host) {
        host.onAttached()
        onDispose { host.onDetached() }
    }
    val horizontalInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Horizontal)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // The width of the whole screen decides how many days fit, before the side insets are taken out.
            .onSizeChanged { host.onWidthChanged(it.width) }
            .windowInsetsPadding(horizontalInsets)
            .background(host.windowBackground)
            .semantics { testTagsAsResourceId = true }
            .testTag("habitListScreen")
    ) {
        Column(Modifier.fillMaxSize()) {
            ListHabitsBar(host.menu, host.selectionMenu, host.barColor, Color.White)
            HeaderContent(host.headerState, host.headerBackground, host.headerText)
            Box(Modifier.fillMaxSize()) {
                HabitListContent(
                    state = host.listState,
                    header = host.headerState,
                    theme = resolveTheme(host.preferences, systemIsDark(Build.VERSION.SDK_INT, isSystemInDarkTheme())),
                    preferences = host.preferences,
                    callbacks = host.rowCallbacks,
                    contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues()
                )
                EmptyListContent(host.emptyState.value)
                AndroidView(
                    factory = { host.progressBar },
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).offset(y = (-6).dp)
                )
                Box(Modifier.align(Alignment.BottomCenter)) { HintContent(host.hintList) }
            }
        }
    }
}
