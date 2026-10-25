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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.archive
import io.github.crisacm.salem.ui.resources.delete
import io.github.crisacm.salem.ui.resources.edit
import io.github.crisacm.salem.ui.resources.export
import io.github.crisacm.salem.ui.resources.more_options
import io.github.crisacm.salem.ui.resources.navigate_up
import io.github.crisacm.salem.ui.resources.randomize
import io.github.crisacm.salem.ui.resources.unarchive
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The top bar of the detail screen: the habit name, the way back, Edit and a menu with the other actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailBar(session: HabitDetailSession, title: String, barColor: Color, onBack: () -> Unit) {
    val menu = session.menu
    val onBarColor = Color.White
    Surface(color = barColor, contentColor = onBarColor, shadowElevation = 2.dp) {
        TopAppBar(
            title = { Text(title, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("showHabitBack")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.navigate_up))
                }
            },
            actions = {
                IconButton(onClick = menu::onEditHabit, modifier = Modifier.testTag("action_edit_habit")) {
                    Icon(Icons.Filled.Edit, stringResource(Res.string.edit))
                }
                var expanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { expanded = true }, modifier = Modifier.testTag("overflowMenu")) {
                        Icon(Icons.Filled.MoreVert, stringResource(Res.string.more_options))
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        MenuItem(Res.string.export, "export") {
                            expanded = false
                            session.onExport()
                        }
                        if (session.canArchive) {
                            MenuItem(Res.string.archive, "action_archive_habit") {
                                expanded = false
                                menu.onArchiveHabits()
                            }
                        }
                        if (session.canUnarchive) {
                            MenuItem(Res.string.unarchive, "action_unarchive_habit") {
                                expanded = false
                                menu.onUnarchiveHabits()
                            }
                        }
                        MenuItem(Res.string.delete, "action_delete") {
                            expanded = false
                            menu.onDeleteHabit()
                        }
                        if (session.isDeveloper) {
                            MenuItem(Res.string.randomize, "action_randomize") {
                                expanded = false
                                menu.onRandomize()
                            }
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = onBarColor,
                navigationIconContentColor = onBarColor,
                actionIconContentColor = onBarColor
            ),
            // The screen already pads the sides, so the bar only needs the top inset.
            windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
        )
    }
}

@Composable
private fun MenuItem(label: StringResource, tag: String, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(stringResource(label)) }, onClick = onClick, modifier = Modifier.testTag(tag))
}
