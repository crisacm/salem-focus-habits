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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.ui.compose.ExpressiveMenu
import io.github.crisacm.salem.ui.compose.ExpressiveMenuGap
import io.github.crisacm.salem.ui.compose.ExpressiveMenuItem
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.about
import io.github.crisacm.salem.ui.resources.action_settings
import io.github.crisacm.salem.ui.resources.archive
import io.github.crisacm.salem.ui.resources.by_color
import io.github.crisacm.salem.ui.resources.by_name
import io.github.crisacm.salem.ui.resources.by_score
import io.github.crisacm.salem.ui.resources.by_status
import io.github.crisacm.salem.ui.resources.close
import io.github.crisacm.salem.ui.resources.color_picker_default_title
import io.github.crisacm.salem.ui.resources.delete
import io.github.crisacm.salem.ui.resources.edit
import io.github.crisacm.salem.ui.resources.filter
import io.github.crisacm.salem.ui.resources.help
import io.github.crisacm.salem.ui.resources.hide_archived
import io.github.crisacm.salem.ui.resources.hide_completed
import io.github.crisacm.salem.ui.resources.hide_entered
import io.github.crisacm.salem.ui.resources.main_activity_title
import io.github.crisacm.salem.ui.resources.manually
import io.github.crisacm.salem.ui.resources.more_options
import io.github.crisacm.salem.ui.resources.night_mode
import io.github.crisacm.salem.ui.resources.reminder
import io.github.crisacm.salem.ui.resources.search
import io.github.crisacm.salem.ui.resources.sort
import io.github.crisacm.salem.ui.resources.unarchive
import org.jetbrains.compose.resources.stringResource

/**
 * The top bar of the habit list. It shows the large title with the filter and the more buttons, a search field
 * while searching, or the number of selected habits and their actions while there is a selection. The
 * `testTag`s are exposed as resource ids for UiAutomator.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ListHabitsBar(
    menu: ListHabitsMenu,
    selection: ListHabitsSelectionMenu,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = selection.active) { selection.cancel() }
    BackHandler(enabled = menu.searchActive && !selection.active) { menu.closeSearch() }
    val scheme = MaterialTheme.colorScheme
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        titleContentColor = scheme.onSurface,
        navigationIconContentColor = scheme.onSurface,
        actionIconContentColor = scheme.onSurfaceVariant
    )
    Surface(
        color = if (selection.active || menu.searchActive) scheme.surfaceContainerHigh else scheme.background,
        contentColor = scheme.onSurface,
        modifier = modifier.exposeTestTags()
    ) {
        when {
            selection.active -> SelectionBar(selection, colors)
            menu.searchActive -> SearchBar(menu, colors)
            else -> MainBar(menu)
        }
    }
}

// The root view already pads the screen on the left and right, so the bars only need the top inset.
@Composable
private fun barInsets() = WindowInsets.systemBars.only(WindowInsetsSides.Top)

/** A round button on a tonal surface, as the filter and the more buttons of the title. */
@Composable
private fun TonalCircleButton(onClick: () -> Unit, tag: String, content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(containerColor = scheme.surfaceContainerHigh, contentColor = scheme.onSurface),
        modifier = Modifier.size(48.dp).testTag(tag),
        content = content
    )
}

@Composable
private fun MainBar(menu: ListHabitsMenu) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(barInsets())
            .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
    ) {
        Text(
            stringResource(Res.string.main_activity_title),
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp,
            modifier = Modifier.weight(1f)
        )
        FilterMenu(menu)
        OverflowMenu(menu)
    }
}

@Composable
private fun FilterMenu(menu: ListHabitsMenu) {
    var expanded by remember { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    // The checked states are read again every time the menu is opened.
    val snapshot = remember(expanded) { menu.snapshot() }
    Box {
        TonalCircleButton(
            onClick = {
                sortOpen = false
                expanded = true
            },
            tag = "action_filter"
        ) {
            Icon(rememberVectorPainter(ListIcons.Filter), stringResource(Res.string.filter))
        }
        ExpressiveMenu(expanded = expanded, onDismiss = { expanded = false }) {
            ExpressiveMenuItem(
                label = stringResource(Res.string.hide_archived),
                icon = Icons.Outlined.Inventory2,
                selected = snapshot.hideArchived,
                onClick = {
                    menu.onToggleHideArchived()
                    expanded = false
                }
            )
            ExpressiveMenuItem(
                label = stringResource(if (snapshot.hideCompletedIsEntered) Res.string.hide_entered else Res.string.hide_completed),
                icon = Icons.Outlined.TaskAlt,
                selected = snapshot.hideCompleted,
                onClick = {
                    menu.onToggleHideCompleted()
                    expanded = false
                }
            )
            ExpressiveMenuGap()
            ExpressiveMenuItem(
                label = stringResource(Res.string.sort),
                icon = Icons.Outlined.SwapVert,
                onClick = { sortOpen = !sortOpen },
                trailing = {
                    Icon(if (sortOpen) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null)
                }
            )
            if (sortOpen) {
                SortItems.forEach { (item, label) ->
                    val mark = sortMark(snapshot.order, item)
                    ExpressiveMenuItem(
                        label = stringResource(label),
                        icon = null,
                        selected = mark != SortMark.NONE,
                        check = false,
                        trailing = { SortIcon(mark) },
                        onClick = {
                            menu.onSort(item)
                            expanded = false
                        }
                    )
                }
            }
            ExpressiveMenuItem(
                label = stringResource(Res.string.search),
                icon = Icons.Outlined.Search,
                onClick = {
                    menu.startSearch()
                    expanded = false
                }
            )
        }
    }
}

private val SortItems = listOf(
    SortItem.MANUAL to Res.string.manually,
    SortItem.NAME to Res.string.by_name,
    SortItem.COLOR to Res.string.by_color,
    SortItem.SCORE to Res.string.by_score,
    SortItem.STATUS to Res.string.by_status
)

@Composable
private fun SortIcon(mark: SortMark) {
    when (mark) {
        SortMark.UP -> Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null)
        SortMark.DOWN -> Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
        SortMark.NONE -> {}
    }
}

@Composable
private fun OverflowMenu(menu: ListHabitsMenu) {
    var expanded by remember { mutableStateOf(false) }
    val snapshot = remember(expanded) { menu.snapshot() }
    Box {
        TonalCircleButton(onClick = { expanded = true }, tag = "overflowMenu") {
            Icon(Icons.Filled.MoreVert, stringResource(Res.string.more_options))
        }
        ExpressiveMenu(expanded = expanded, onDismiss = { expanded = false }) {
            ExpressiveMenuItem(
                label = stringResource(Res.string.night_mode),
                icon = Icons.Outlined.DarkMode,
                selected = snapshot.nightMode,
                onClick = {
                    menu.onToggleNightMode()
                    expanded = false
                }
            )
            ExpressiveMenuGap()
            ExpressiveMenuItem(stringResource(Res.string.action_settings), Icons.Outlined.Settings, {
                menu.onSettings()
                expanded = false
            })
            ExpressiveMenuItem(stringResource(Res.string.help), Icons.Outlined.QuestionMark, {
                menu.onHelp()
                expanded = false
            })
            ExpressiveMenuItem(stringResource(Res.string.about), Icons.Outlined.Info, {
                menu.onAbout()
                expanded = false
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(menu: ListHabitsMenu, colors: androidx.compose.material3.TopAppBarColors) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    TopAppBar(
        title = {
            TextField(
                value = menu.searchQuery,
                onValueChange = menu::onSearchChange,
                placeholder = { Text(stringResource(Res.string.search)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.focusRequester(focus).testTag("searchField")
            )
        },
        actions = {
            IconButton(onClick = menu::closeSearch, modifier = Modifier.testTag("searchClose")) {
                Icon(Icons.Filled.Close, stringResource(Res.string.close))
            }
        },
        colors = colors,
        windowInsets = barInsets()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionBar(selection: ListHabitsSelectionMenu, colors: androidx.compose.material3.TopAppBarColors) {
    var overflow by remember { mutableStateOf(false) }
    val actions = selection.actions
    TopAppBar(
        title = { Text(selection.selectedCount.toString(), modifier = Modifier.testTag("selectionCount")) },
        navigationIcon = {
            IconButton(onClick = selection::cancel, modifier = Modifier.testTag("selectionClose")) {
                Icon(Icons.Filled.Close, stringResource(Res.string.close))
            }
        },
        actions = {
            if (actions.canEdit) {
                IconButton(onClick = selection::onEdit, modifier = Modifier.testTag("action_edit_habit")) {
                    Icon(rememberVectorPainter(ListIcons.Edit), stringResource(Res.string.edit))
                }
            }
            if (actions.canColor) {
                IconButton(onClick = selection::onColor, modifier = Modifier.testTag("action_color")) {
                    Icon(rememberVectorPainter(ListIcons.Palette), stringResource(Res.string.color_picker_default_title))
                }
            }
            Box {
                IconButton(onClick = { overflow = true }, modifier = Modifier.testTag("selectionOverflow")) {
                    Icon(Icons.Filled.MoreVert, stringResource(Res.string.more_options))
                }
                ExpressiveMenu(expanded = overflow, onDismiss = { overflow = false }) {
                    if (actions.canArchive) {
                        ExpressiveMenuItem(stringResource(Res.string.archive), Icons.Outlined.Archive, {
                            selection.onArchive()
                            overflow = false
                        })
                    }
                    if (actions.canUnarchive) {
                        ExpressiveMenuItem(stringResource(Res.string.unarchive), Icons.Outlined.Unarchive, {
                            selection.onUnarchive()
                            overflow = false
                        })
                    }
                    if (actions.showNotify) {
                        ExpressiveMenuItem(stringResource(Res.string.reminder), Icons.Outlined.Notifications, {
                            selection.onNotify()
                            overflow = false
                        })
                    }
                    ExpressiveMenuGap()
                    ExpressiveMenuItem(stringResource(Res.string.delete), Icons.Outlined.Delete, {
                        selection.onDelete()
                        overflow = false
                    }, destructive = true)
                }
            }
        },
        colors = colors,
        windowInsets = barInsets()
    )
}
