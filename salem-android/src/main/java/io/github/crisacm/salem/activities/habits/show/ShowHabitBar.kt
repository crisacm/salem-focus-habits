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

package io.github.crisacm.salem.activities.habits.show

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.R

/** The top bar of the detail screen: the habit name, the way back, Edit and a menu with the other actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowHabitBar(host: ShowHabitHost, title: String, barColor: Color, onBack: () -> Unit) {
    val presenter = host.menuPresenter
    val onBarColor = Color.White
    Surface(color = barColor, contentColor = onBarColor, shadowElevation = 2.dp) {
        TopAppBar(
            title = { Text(title, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("showHabitBack")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_up))
                }
            },
            actions = {
                IconButton(onClick = presenter::onEditHabit, modifier = Modifier.testTag("action_edit_habit")) {
                    Icon(painterResource(R.drawable.ic_action_edit_dark), stringResource(R.string.edit))
                }
                var expanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { expanded = true }, modifier = Modifier.testTag("overflowMenu")) {
                        Icon(Icons.Filled.MoreVert, stringResource(R.string.more_options))
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        MenuItem(R.string.export, "export") {
                            expanded = false
                            presenter.onExportCSV()
                        }
                        if (host.canArchive) {
                            MenuItem(R.string.archive, "action_archive_habit") {
                                expanded = false
                                presenter.onArchiveHabits()
                            }
                        }
                        if (host.canUnarchive) {
                            MenuItem(R.string.unarchive, "action_unarchive_habit") {
                                expanded = false
                                presenter.onUnarchiveHabits()
                            }
                        }
                        MenuItem(R.string.delete, "action_delete") {
                            expanded = false
                            presenter.onDeleteHabit()
                        }
                        if (host.isDeveloper) {
                            MenuItem(R.string.randomize, "action_randomize") {
                                expanded = false
                                presenter.onRandomize()
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
private fun MenuItem(label: Int, tag: String, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(stringResource(label)) }, onClick = onClick, modifier = Modifier.testTag(tag))
}
