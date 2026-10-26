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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import io.github.crisacm.salem.platform.notifications.AppLifecycleObserver
import io.github.crisacm.salem.platform.time.AppleLocalDateFormatter
import io.github.crisacm.salem.platform.time.computeToday
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.platform.time.setFirstWeekday
import io.github.crisacm.salem.platform.time.setToday
import io.github.crisacm.salem.ui.AppScreen
import io.github.crisacm.salem.ui.about.AboutRoute
import io.github.crisacm.salem.ui.about.SalemLinks
import io.github.crisacm.salem.ui.compose.SalemTheme
import io.github.crisacm.salem.ui.compose.resolveTheme
import io.github.crisacm.salem.ui.detail.HabitDetailRoute
import io.github.crisacm.salem.ui.intro.IntroContent
import io.github.crisacm.salem.ui.list.ListHabitsContent
import io.github.crisacm.salem.ui.list.ListOverlayHost
import io.github.crisacm.salem.ui.list.hiddenWhileOverlay
import io.github.crisacm.salem.ui.settings.SettingsContent
import io.github.crisacm.salem.ui.settings.SettingsState
import io.github.crisacm.salem.ui.settings.weekdayNamesFromSaturday
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

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
        } catch (e: CancellationException) {
            throw e
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

@OptIn(ExperimentalNativeApi::class)
@Composable
private fun HabitListScreen(graph: IosHabitsGraph) {
    val density = LocalDensity.current.density
    val model = remember(graph, density) { IosListScreenModel(graph, density) }
    val systemIsDark = isSystemInDarkTheme()
    // Reading the version makes the screen compose again when the user toggles the night mode
    graph.screen.themeVersion
    val theme = resolveTheme(graph.preferences, systemIsDark)
    model.theme = theme
    LaunchedEffect(systemIsDark) {
        graph.themeSwitcher.systemIsDark = systemIsDark
        graph.screen.applyTheme()
    }
    // The first launch of the app and the intro are decided on the main thread, once the screen is shown
    LaunchedEffect(graph) { graph.behavior.onStartup() }

    DisposableEffect(graph) {
        graph.midnightTimer.onResume()
        // Back from the background the day may have changed
        val observer = AppLifecycleObserver(
            onForeground = {
                setToday(computeToday(graph.preferences.midnightDelayHours, 0))
                setFirstWeekday(graph.preferences.firstWeekday)
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
    // What a change in Settings needs to show on the list: the theme, the day and the first weekday, the header and the rows
    val onSettingsChanged = {
        graph.screen.applyTheme()
        setToday(computeToday(graph.preferences.midnightDelayHours, 0))
        setFirstWeekday(graph.preferences.firstWeekday)
        model.headerState.today = getToday()
        graph.listState.refresh()
    }
    SalemTheme(theme) {
        Box(Modifier.fillMaxSize()) {
            val detailOpen = graph.navigation.habit != null
            val screenOpen = graph.screens.openScreens.isNotEmpty()
            ListHabitsContent(
                model,
                theme,
                // The list stays under the detail: hide it from VoiceOver while the detail is open
                modifier = Modifier.hiddenWhileOverlay(graph.overlays).then(if (detailOpen || screenOpen) Modifier.clearAndSetSemantics { } else Modifier)
            )
            ListOverlayHost(
                overlays = graph.overlays,
                theme = theme,
                dateFormatter = remember { AppleLocalDateFormatter() },
                preferences = graph.preferences,
                habitList = graph.habitList,
                saverFactory = graph.saverFactory,
                decimalSeparator = remember { iosDecimalSeparator() }
            )
            graph.navigation.habit?.let { habit ->
                HabitDetailRoute(
                    habit = habit,
                    theme = theme,
                    dateFormatter = remember { AppleLocalDateFormatter() },
                    habitList = graph.habitList,
                    preferences = graph.preferences,
                    commandRunner = graph.commandRunner,
                    taskRunner = graph.taskRunner,
                    system = graph.dirFinder,
                    saverFactory = graph.saverFactory,
                    decimalSeparator = remember { iosDecimalSeparator() },
                    onClose = graph.navigation::close,
                    developer = Platform.isDebugBinary
                )
            }
            val openScreens = graph.screens.openScreens
            openScreens.forEachIndexed { index, screen ->
                key(screen) {
                    // Only the screen on top is read by VoiceOver; the ones below it stay composed to keep their state
                    Box(Modifier.fillMaxSize().then(if (index < openScreens.lastIndex) Modifier.clearAndSetSemantics { } else Modifier)) {
                        when (screen) {
                            AppScreen.ABOUT -> AboutRoute(
                                versionName = remember { iosVersionName() },
                                preferences = graph.preferences,
                                openUrl = ::openUrl,
                                onBack = { graph.screens.close(AppScreen.ABOUT) }
                            )
                            AppScreen.INTRO -> IntroContent(onFinish = { graph.screens.close(AppScreen.INTRO) })
                            AppScreen.SETTINGS -> {
                                val dateFormatter = remember { AppleLocalDateFormatter() }
                                val weekdayNames = remember { weekdayNamesFromSaturday(dateFormatter::longWeekdayName) }
                                val state = remember(model) { SettingsState(graph.preferences, onSettingsChanged) }
                                // About may have turned the developer mode on while this screen was below it
                                LaunchedEffect(openScreens.size) { state.refresh() }
                                SettingsContent(
                                    state = state,
                                    weekdayNames = weekdayNames,
                                    // There is no confetti on iOS yet
                                    showAnimationsSwitch = false,
                                    onBack = { graph.screens.close(AppScreen.SETTINGS) },
                                    onHelp = { openUrl(SalemLinks.HELP) },
                                    onRate = { openUrl(SalemLinks.RATE) },
                                    onAbout = { graph.screens.open(AppScreen.ABOUT) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
