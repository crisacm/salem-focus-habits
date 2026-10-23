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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.R

/**
 * The top bar of the habit list. It shows the title and the actions, a search field while searching,
 * or the number of selected habits and their actions while there is a selection. The `testTag`s are
 * exposed as resource ids for UiAutomator.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ListHabitsBar(
    menu: ListHabitsMenu,
    selection: ListHabitsSelectionMenu,
    barColor: Color,
    onBarColor: Color
) {
    BackHandler(enabled = selection.active) { selection.cancel() }
    BackHandler(enabled = menu.searchActive && !selection.active) { menu.closeSearch() }
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        titleContentColor = onBarColor,
        navigationIconContentColor = onBarColor,
        actionIconContentColor = onBarColor
    )
    Surface(
        color = barColor,
        contentColor = onBarColor,
        shadowElevation = 2.dp,
        modifier = Modifier.semantics { testTagsAsResourceId = true }
    ) {
        when {
            selection.active -> SelectionBar(selection, colors)
            menu.searchActive -> SearchBar(menu, colors)
            else -> MainBar(menu, colors)
        }
    }
}

// The root view already pads the screen on the left and right, so the bars only need the top inset.
@Composable
private fun barInsets() = WindowInsets.systemBars.only(WindowInsetsSides.Top)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainBar(menu: ListHabitsMenu, colors: androidx.compose.material3.TopAppBarColors) {
    TopAppBar(
        title = { Text(stringResource(R.string.main_activity_title)) },
        actions = {
            IconButton(onClick = menu::onCreateHabit, modifier = Modifier.testTag("actionCreateHabit")) {
                Icon(painterResource(R.drawable.ic_action_add_dark), stringResource(R.string.add_habit))
            }
            FilterMenu(menu)
            OverflowMenu(menu)
        },
        colors = colors,
        windowInsets = barInsets()
    )
}

@Composable
private fun FilterMenu(menu: ListHabitsMenu) {
    var expanded by remember { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    // The checked states are read again every time the menu is opened.
    val snapshot = remember(expanded) { menu.snapshot() }
    Box {
        IconButton(
            onClick = {
                sortOpen = false
                expanded = true
            },
            modifier = Modifier.testTag("action_filter")
        ) {
            Icon(painterResource(R.drawable.ic_action_filter_dark), stringResource(R.string.filter))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CheckItem(R.string.hide_archived, snapshot.hideArchived) {
                menu.onToggleHideArchived()
                expanded = false
            }
            CheckItem(
                if (snapshot.hideCompletedIsEntered) R.string.hide_entered else R.string.hide_completed,
                snapshot.hideCompleted
            ) {
                menu.onToggleHideCompleted()
                expanded = false
            }
            DropdownMenuItem(text = { Text(stringResource(R.string.sort)) }, onClick = { sortOpen = !sortOpen })
            if (sortOpen) {
                SortItems.forEach { (item, label) ->
                    DropdownMenuItem(
                        text = { Text(stringResource(label)) },
                        trailingIcon = { SortIcon(sortMark(snapshot.order, item)) },
                        onClick = {
                            menu.onSort(item)
                            expanded = false
                        }
                    )
                }
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.search)) },
                onClick = {
                    menu.startSearch()
                    expanded = false
                }
            )
        }
    }
}

private val SortItems = listOf(
    SortItem.MANUAL to R.string.manually,
    SortItem.NAME to R.string.by_name,
    SortItem.COLOR to R.string.by_color,
    SortItem.SCORE to R.string.by_score,
    SortItem.STATUS to R.string.by_status
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
private fun CheckItem(label: Int, checked: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(label)) },
        trailingIcon = { if (checked) Icon(Icons.Filled.Check, contentDescription = null) },
        onClick = onClick
    )
}

@Composable
private fun OverflowMenu(menu: ListHabitsMenu) {
    var expanded by remember { mutableStateOf(false) }
    val snapshot = remember(expanded) { menu.snapshot() }
    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.testTag("overflowMenu")) {
            Icon(Icons.Filled.MoreVert, stringResource(R.string.more_options))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CheckItem(R.string.night_mode, snapshot.nightMode) {
                menu.onToggleNightMode()
                expanded = false
            }
            MenuItem(R.string.action_settings) {
                menu.onSettings()
                expanded = false
            }
            MenuItem(R.string.help) {
                menu.onHelp()
                expanded = false
            }
            MenuItem(R.string.about) {
                menu.onAbout()
                expanded = false
            }
        }
    }
}

@Composable
private fun MenuItem(label: Int, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(stringResource(label)) }, onClick = onClick)
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
                placeholder = { Text(stringResource(R.string.search)) },
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
                Icon(Icons.Filled.Close, stringResource(R.string.close))
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
                Icon(Icons.Filled.Close, stringResource(R.string.close))
            }
        },
        actions = {
            if (actions.canEdit) {
                IconButton(onClick = selection::onEdit, modifier = Modifier.testTag("action_edit_habit")) {
                    Icon(painterResource(R.drawable.ic_action_edit_dark), stringResource(R.string.edit))
                }
            }
            IconButton(onClick = selection::onColor, modifier = Modifier.testTag("action_color")) {
                Icon(painterResource(R.drawable.ic_action_color_dark), stringResource(R.string.color_picker_default_title))
            }
            Box {
                IconButton(onClick = { overflow = true }, modifier = Modifier.testTag("selectionOverflow")) {
                    Icon(Icons.Filled.MoreVert, stringResource(R.string.more_options))
                }
                DropdownMenu(expanded = overflow, onDismissRequest = { overflow = false }) {
                    if (actions.canArchive) {
                        MenuItem(R.string.archive) {
                            selection.onArchive()
                            overflow = false
                        }
                    }
                    if (actions.canUnarchive) {
                        MenuItem(R.string.unarchive) {
                            selection.onUnarchive()
                            overflow = false
                        }
                    }
                    MenuItem(R.string.delete) {
                        selection.onDelete()
                        overflow = false
                    }
                    if (actions.showNotify) {
                        MenuItem(R.string.reminder) {
                            selection.onNotify()
                            overflow = false
                        }
                    }
                }
            }
        },
        colors = colors,
        windowInsets = barInsets()
    )
}
