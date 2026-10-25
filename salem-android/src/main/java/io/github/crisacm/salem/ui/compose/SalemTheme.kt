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

package io.github.crisacm.salem.ui.compose

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.crisacm.salem.HabitsApplication

// The colours, the scheme, SalemTheme(theme) and resolveTheme are shared: they live in salem-ui, same package

/**
 * Whether the system is in night mode as [io.github.crisacm.salem.activities.AndroidThemeSwitcher] sees
 * it: before Android 10 the activities never follow the system, so neither do the dialogs.
 */
fun systemIsDark(sdkInt: Int, nightMode: Boolean) = sdkInt >= 29 && nightMode

/** [SalemTheme] using the theme chosen in the preferences, for views hosted outside an activity theme switch. */
@Composable
fun SalemTheme(content: @Composable () -> Unit) {
    val preferences = (LocalContext.current.applicationContext as HabitsApplication).component.preferences
    SalemTheme(resolveTheme(preferences, systemIsDark(Build.VERSION.SDK_INT, isSystemInDarkTheme())), content)
}
