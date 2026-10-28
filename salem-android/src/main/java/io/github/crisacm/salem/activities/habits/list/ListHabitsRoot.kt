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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import io.github.crisacm.salem.ui.compose.resolveTheme
import io.github.crisacm.salem.ui.compose.systemIsDark
import io.github.crisacm.salem.ui.list.ListHabitsContent

/**
 * The shared habit list screen with what only Android provides: the theme
 * as the activity resolves it, and the side insets that the confetti View over the screen needs.
 */
@Composable
fun ListHabitsRoot(host: ListHabitsHost) {
    ListHabitsContent(
        model = host,
        theme = resolveTheme(host.preferences, systemIsDark(Build.VERSION.SDK_INT, isSystemInDarkTheme())),
        onSideInsets = host::onSideInsets
    )
}
