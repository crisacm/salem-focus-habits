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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.ui.screens.habits.edit.HabitEditorSaver
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.compose.toCompose
import io.github.crisacm.salem.ui.list.ListOverlayHost
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.could_not_export
import io.github.crisacm.salem.ui.resources.toast_habits_archived
import io.github.crisacm.salem.ui.resources.toast_habits_unarchived
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val MESSAGE_MILLIS = 3_000L

/**
 * The detail of [habit] as a platform that draws its own dialogs shows it: the screen, the history editor, the popups,
 * the editor and the messages over it. Back closes the topmost one and then calls [onClose]. The top bar has the colour
 * of the habit, and Export only says that it is not available. [developer] shows Randomize in the menu.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HabitDetailRoute(
    habit: Habit,
    theme: Theme,
    dateFormatter: LocalDateFormatter,
    habitList: HabitList,
    preferences: Preferences,
    commandRunner: CommandRunner,
    taskRunner: TaskRunner,
    system: ShowHabitMenuPresenter.System,
    saverFactory: () -> HabitEditorSaver,
    decimalSeparator: Char,
    share: (String) -> Unit,
    onClose: () -> Unit,
    developer: Boolean = false
) {
    val currentOnClose by rememberUpdatedState(onClose)
    val currentShare by rememberUpdatedState(share)
    val currentTheme by rememberUpdatedState(theme)
    val layers = remember(habit) { HabitDetailLayers(onClose = { currentOnClose() }, onShare = { currentShare(it) }) }
    val scope = rememberCoroutineScope()
    val session = remember(habit) {
        HabitDetailSession(
            habit = habit,
            habitList = habitList,
            preferences = preferences,
            commandRunner = commandRunner,
            taskRunner = taskRunner,
            system = system,
            theme = { currentTheme },
            hooks = layers,
            scope = scope,
            developerOptions = developer
        )
    }
    DisposableEffect(session) {
        session.attach()
        onDispose { session.detach() }
    }
    // The cards keep the theme they were built with: build them again when the kind of theme changes. The kind is the
    // class because a theme is a new object on every composition.
    val themeKind = theme::class
    var shownThemeKind by remember(session) { mutableStateOf(themeKind) }
    LaunchedEffect(themeKind) {
        if (themeKind != shownThemeKind) {
            shownThemeKind = themeKind
            session.refresh()
        }
    }

    // Composed first, so it is the last one asked: the history editor and the overlays below take back before it
    BackHandler(onBack = currentOnClose)
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .then(if (layers.isCovering) Modifier.clearAndSetSemantics { } else Modifier)
        ) {
            HabitDetailScreen(session, theme.color(habit.color).toCompose(), dateFormatter, currentOnClose)
        }
        HistoryEditorHost(layers.historyEditor, session.state?.history, dateFormatter)
        ListOverlayHost(
            overlays = layers.overlays,
            theme = theme,
            dateFormatter = dateFormatter,
            preferences = preferences,
            habitList = habitList,
            saverFactory = saverFactory,
            decimalSeparator = decimalSeparator
        )
        DetailMessageBanner(layers)
    }
}

/** Shows the message of [layers] for a few seconds at the bottom of the screen. */
@Composable
fun DetailMessageBanner(layers: HabitDetailLayers) {
    val message = layers.message ?: return
    LaunchedEffect(layers.messageVersion) {
        delay(MESSAGE_MILLIS)
        layers.clearMessage()
    }
    val text = when (message) {
        DetailMessage.ARCHIVED -> pluralStringResource(Res.plurals.toast_habits_archived, 1)
        DetailMessage.UNARCHIVED -> pluralStringResource(Res.plurals.toast_habits_unarchived, 1)
        DetailMessage.COULD_NOT_EXPORT -> stringResource(Res.string.could_not_export)
    }
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.BottomCenter) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.inverseSurface,
            shadowElevation = 6.dp,
            modifier = Modifier.padding(16.dp).testTag("detailMessage")
        ) {
            Text(
                text,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}
