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

package io.github.crisacm.salem.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.compose.toExpressiveColorScheme
import io.github.crisacm.salem.ui.list.exposeTestTags

/**
 * The whole detail screen of a habit in one composition: the header with the actions and the cards. [onBack] leaves
 * the screen. The colours come from the theme of the state, so nothing is drawn but black until the state exists.
 */
@Composable
fun HabitDetailScreen(
    session: HabitDetailSession,
    dateFormatter: LocalDateFormatter,
    onBack: () -> Unit
) {
    val state = session.state
    val sideInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    if (state == null) {
        // The black screen is drawn from the first frame, before the state exists, so it covers what is under it
        Column(Modifier.fillMaxSize().background(Color.Black).exposeTestTags().testTag("showHabitScreen")) {}
        return
    }
    MaterialTheme(colorScheme = state.theme.toExpressiveColorScheme()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(sideInsets)
                .exposeTestTags()
                .testTag("showHabitScreen")
        ) {
            HabitDetailContent(
                state,
                session.callbacks,
                dateFormatter,
                Modifier.weight(1f),
                header = { HabitDetailHeader(session, state, onBack) }
            )
        }
    }
}
