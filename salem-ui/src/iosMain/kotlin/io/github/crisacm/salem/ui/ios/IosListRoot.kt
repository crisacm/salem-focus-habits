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

package io.github.crisacm.salem.ui.ios

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import io.github.crisacm.salem.platform.notifications.AppLifecycleObserver
import io.github.crisacm.salem.platform.time.computeToday
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.platform.time.setToday
import io.github.crisacm.salem.ui.compose.SalemTheme
import io.github.crisacm.salem.ui.compose.resolveTheme
import io.github.crisacm.salem.ui.list.ListHabitsContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private sealed interface Loading {
    data object InProgress : Loading

    class Ready(val graph: IosHabitsGraph) : Loading

    class Failed(val error: Throwable) : Loading
}

/** The root of the iOS app: opens the database off the main thread and then shows the habit list. */
@Composable
fun IosListRoot() {
    var loading by remember { mutableStateOf<Loading>(Loading.InProgress) }
    LaunchedEffect(Unit) {
        loading = try {
            Loading.Ready(withContext(Dispatchers.Default) { IosHabitsGraph.create() })
        } catch (e: Exception) {
            Loading.Failed(e)
        }
    }
    when (val state = loading) {
        Loading.InProgress -> Unit
        is Loading.Failed -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Could not open the habits: ${state.error.message ?: state.error::class.simpleName}")
        }
        is Loading.Ready -> HabitListScreen(state.graph)
    }
}

@Composable
private fun HabitListScreen(graph: IosHabitsGraph) {
    val density = LocalDensity.current.density
    val model = remember(graph, density) { IosListScreenModel(graph, density) }
    val systemIsDark = isSystemInDarkTheme()
    // Reading the version makes the screen compose again when the user toggles the night mode
    graph.screen.themeVersion
    val theme = resolveTheme(graph.preferences, systemIsDark)
    model.theme = theme
    LaunchedEffect(systemIsDark) { graph.screen.applyTheme() }

    DisposableEffect(graph) {
        graph.midnightTimer.onResume()
        // Back from the background the day may have changed
        val observer = AppLifecycleObserver(
            onForeground = {
                setToday(computeToday(graph.preferences.midnightDelayHours, 0))
                model.headerState.today = getToday()
                graph.midnightTimer.onResume()
                graph.listState.refresh()
            },
            onBackground = {
                graph.midnightTimer.onPause()
                graph.listState.cancelRefresh()
            }
        )
        onDispose {
            observer.stop()
            graph.midnightTimer.onPause()
        }
    }
    SalemTheme(theme) { ListHabitsContent(model, theme) }
}
