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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.list.exposeTestTags

/**
 * The whole detail screen of a habit in one composition: the top bar and the cards. [barColor] is the colour of the
 * top bar, which the platform chooses; [onBack] leaves the screen.
 */
@Composable
fun HabitDetailScreen(
    session: HabitDetailSession,
    barColor: Color,
    dateFormatter: LocalDateFormatter,
    onBack: () -> Unit
) {
    val state = session.state ?: return
    val sideInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(sideInsets)
            .exposeTestTags()
            .testTag("showHabitScreen")
    ) {
        HabitDetailBar(session, state.title, barColor, onBack)
        HabitDetailContent(state, session.callbacks, dateFormatter, Modifier.weight(1f))
    }
}
