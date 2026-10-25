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

package io.github.crisacm.salem.ui.about

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.you_are_now_a_developer
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * [AboutContent] with its state: five taps on the version turn on the developer mode in [preferences] and say so. Back
 * calls [onBack].
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AboutRoute(
    versionName: String,
    preferences: Preferences,
    openUrl: (String) -> Unit,
    onBack: () -> Unit
) {
    val countdown = remember { DeveloperCountdown() }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val unlocked = stringResource(Res.string.you_are_now_a_developer)
    val currentOnBack = rememberUpdatedState(onBack)
    BackHandler { currentOnBack.value() }
    AboutContent(
        versionName = versionName,
        onBack = { currentOnBack.value() },
        onVersionTap = {
            if (countdown.onTap()) {
                preferences.isDeveloper = true
                scope.launch { snackbar.showSnackbar(unlocked) }
            }
        },
        openUrl = openUrl,
        snackbar = snackbar
    )
}
