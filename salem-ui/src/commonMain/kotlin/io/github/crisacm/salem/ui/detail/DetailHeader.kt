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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitState
import io.github.crisacm.salem.ui.compose.ExpressiveMenu
import io.github.crisacm.salem.ui.compose.ExpressiveMenuGap
import io.github.crisacm.salem.ui.compose.ExpressiveMenuItem
import io.github.crisacm.salem.ui.edit.ScallopShape
import io.github.crisacm.salem.ui.list.HabitTones
import io.github.crisacm.salem.ui.list.habitIconFor
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

/**
 * The top of the detail screen: a rounded panel in the tones of the habit with the buttons to go back, edit and open
 * the menu, the icon and the name of the habit, and its question, frequency and reminder.
 */
@Composable
fun HabitDetailHeader(session: HabitDetailSession, state: ShowHabitState, onBack: () -> Unit) {
    val menu = session.menu
    val tones = rememberDetailTones(state.theme, state.color)
    var expanded by remember { mutableStateOf(false) }
    Surface(
        color = tones.container,
        shape = RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 27.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RoundButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(Res.string.navigate_up), tones, "showHabitBack", onBack)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoundButton(Icons.Filled.Edit, stringResource(Res.string.edit), tones, "action_edit_habit", menu::onEditHabit)
                    Box {
                        RoundButton(Icons.Filled.MoreVert, stringResource(Res.string.more_options), tones, "overflowMenu") { expanded = true }
                        ExpressiveMenu(expanded = expanded, onDismiss = { expanded = false }) {
                            MenuItem(Res.string.export, Icons.Outlined.FileDownload, "export") {
                                expanded = false
                                session.onExport()
                            }
                            if (session.canArchive) {
                                MenuItem(Res.string.archive, Icons.Outlined.Archive, "action_archive_habit") {
                                    expanded = false
                                    menu.onArchiveHabits()
                                }
                            }
                            if (session.canUnarchive) {
                                MenuItem(Res.string.unarchive, Icons.Outlined.Unarchive, "action_unarchive_habit") {
                                    expanded = false
                                    menu.onUnarchiveHabits()
                                }
                            }
                            if (session.isDeveloper) {
                                MenuItem(Res.string.randomize, Icons.Outlined.Shuffle, "action_randomize") {
                                    expanded = false
                                    menu.onRandomize()
                                }
                            }
                            ExpressiveMenuGap()
                            MenuItem(Res.string.delete, Icons.Outlined.Delete, "action_delete", destructive = true) {
                                expanded = false
                                menu.onDeleteHabit()
                            }
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(start = 3.dp, top = 20.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(60.dp).clip(ScallopShape()).background(tones.accent)
                ) {
                    Icon(habitIconFor(state.habitUuid, state.habitId), contentDescription = null, tint = tones.onAccent, modifier = Modifier.size(30.dp))
                }
                Text(
                    state.title,
                    color = tones.onContainer,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
            SubtitleCardContent(state.subtitle, Modifier.padding(top = 20.dp))
        }
    }
}

@Composable
private fun RoundButton(icon: ImageVector, description: String, tones: HabitTones, tag: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(tones.chip)
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Icon(icon, contentDescription = description, tint = tones.onContainer, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun MenuItem(label: StringResource, icon: ImageVector, tag: String, destructive: Boolean = false, onClick: () -> Unit) {
    ExpressiveMenuItem(stringResource(label), icon, onClick, Modifier.testTag(tag), destructive = destructive)
}
