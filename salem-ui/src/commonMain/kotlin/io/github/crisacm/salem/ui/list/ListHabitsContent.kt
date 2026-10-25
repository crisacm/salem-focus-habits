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

import androidx.compose.foundation.background
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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.ui.views.Theme

/**
 * The whole habit list screen in one composition: the top bar, the day header, the rows (or the empty
 * state), the task progress bar and the hint. [theme] colours the habits; [onSideInsets] tells a platform view laid
 * over this one (the confetti of Android) the side insets that the composition applied.
 */
@Composable
fun ListHabitsContent(
    model: ListScreenModel,
    theme: Theme,
    modifier: Modifier = Modifier,
    onSideInsets: (left: Int, right: Int) -> Unit = { _, _ -> }
) {
    DisposableEffect(model) {
        model.onAttached()
        onDispose { model.onDetached() }
    }
    val horizontalInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Horizontal)
    // A view over this composition takes the very same side insets, from one source.
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val left = horizontalInsets.getLeft(density, direction)
    val right = horizontalInsets.getRight(density, direction)
    SideEffect { onSideInsets(left, right) }
    val colors = model.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            // The width of the whole screen decides how many days fit, before the side insets are taken out.
            .onSizeChanged { model.onWidthChanged(it.width) }
            .windowInsetsPadding(horizontalInsets)
            .background(colors.windowBackground)
            .exposeTestTags()
            .testTag("habitListScreen")
    ) {
        Column(Modifier.fillMaxSize()) {
            ListHabitsBar(model.menu, model.selectionMenu, colors.barColor, colors.onBarColor)
            HeaderContent(model.headerState, model.dateFormatter, colors.headerBackground, colors.headerText)
            Box(Modifier.fillMaxSize()) {
                HabitListContent(
                    state = model.listState,
                    header = model.headerState,
                    theme = theme,
                    colors = colors.list,
                    preferences = model.preferences,
                    callbacks = model.rowCallbacks,
                    contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues()
                )
                EmptyListContent(model.emptyState.value)
                TaskProgress(model.taskRunner, Modifier.fillMaxWidth().align(Alignment.TopCenter).offset(y = (-6).dp))
                Box(Modifier.align(Alignment.BottomCenter)) { HintContent(model.hintList) }
            }
        }
    }
}
