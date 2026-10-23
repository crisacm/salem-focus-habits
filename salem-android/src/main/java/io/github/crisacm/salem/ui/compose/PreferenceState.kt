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

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.preference.PreferenceManager

/** The default SharedPreferences file, the one used by `SharedPreferencesStorage`. */
@Composable
fun rememberDefaultPreferences(): SharedPreferences {
    val context = LocalContext.current
    return remember(context) { PreferenceManager.getDefaultSharedPreferences(context) }
}

/** State that follows [key] in [prefs]; [read] is evaluated again whenever the key changes. */
@Composable
fun <T> rememberPref(
    prefs: SharedPreferences,
    key: String,
    read: (SharedPreferences) -> T
): State<T> {
    val state = remember(prefs, key) { mutableStateOf(read(prefs)) }
    DisposableEffect(prefs, key) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { changed, changedKey ->
            if (changedKey == key || changedKey == null) state.value = read(changed)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        state.value = read(prefs)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return state
}

@Composable
fun rememberPrefBoolean(prefs: SharedPreferences, key: String, default: Boolean = false) =
    rememberPref(prefs, key) { it.getBoolean(key, default) }

@Composable
fun rememberPrefString(prefs: SharedPreferences, key: String, default: String? = null) =
    rememberPref(prefs, key) { it.getString(key, default) }
