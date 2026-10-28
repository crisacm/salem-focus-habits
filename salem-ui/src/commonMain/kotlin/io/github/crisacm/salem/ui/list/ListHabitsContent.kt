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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.ui.compose.toExpressiveColorScheme
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.new_habit
import org.jetbrains.compose.resources.stringResource

/** The room under the last card, so that the button to create a habit never covers it. */
private val ButtonRoom = 88.dp

/**
 * The whole habit list screen in one composition: the title bar, the days, the cards (or the empty
 * state), the button to create a habit, the task progress bar and the hint. [theme] chooses the colours of the
 * screen and the tones of the cards; [onSideInsets] tells a platform view laid over this one (the confetti of
 * Android) the side insets that the composition applied.
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
    MaterialTheme(colorScheme = theme.toExpressiveColorScheme()) {
        val scheme = MaterialTheme.colorScheme
        val browsing = !model.selectionMenu.active && !model.menu.searchActive
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                // The width of the whole screen tells the model when the screen is ready, before the side insets are taken out.
                .onSizeChanged { model.onWidthChanged(it.width) }
                .windowInsetsPadding(horizontalInsets)
                .background(scheme.background)
                .exposeTestTags()
                .testTag("habitListScreen")
        ) {
            Column(Modifier.fillMaxSize()) {
                ListHabitsBar(model.menu, model.selectionMenu)
                HeaderContent(model.headerState, model.dateFormatter)
                Box(Modifier.fillMaxSize()) {
                    HabitListContent(
                        state = model.listState,
                        header = model.headerState,
                        theme = theme,
                        preferences = model.preferences,
                        callbacks = model.rowCallbacks,
                        contentPadding = PaddingValues(
                            bottom = WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues().calculateBottomPadding() + ButtonRoom
                        )
                    )
                    EmptyListContent(model.emptyState.value)
                    TaskProgress(model.taskRunner, Modifier.fillMaxWidth().align(Alignment.TopCenter))
                    Box(Modifier.align(Alignment.BottomCenter).padding(bottom = if (browsing) ButtonRoom else 0.dp)) {
                        HintContent(model.hintList)
                    }
                }
            }
            if (browsing) {
                ExtendedFloatingActionButton(
                    onClick = model.menu::onCreateHabit,
                    containerColor = scheme.secondaryContainer,
                    contentColor = scheme.onSecondaryContainer,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                        .padding(16.dp)
                        .testTag("actionCreateHabit")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(
                        stringResource(Res.string.new_habit),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
    }
}
