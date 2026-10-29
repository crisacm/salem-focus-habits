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

package io.github.crisacm.salem.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.ui.compose.ChoiceOption
import io.github.crisacm.salem.ui.dialogs.closeButtonColor
import io.github.crisacm.salem.ui.edit.isDarkScheme
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.cancel
import io.github.crisacm.salem.ui.resources.navigate_up
import io.github.crisacm.salem.ui.resources.settings
import org.jetbrains.compose.resources.stringResource

/** The colour family of a section of Settings: its title, and the badge and the icon of its rows. */
enum class SettingsTone(
    private val darkBadge: Long,
    private val darkOnBadge: Long,
    private val darkTitle: Long,
    private val lightBadge: Long,
    private val lightOnBadge: Long,
    private val lightTitle: Long
) {
    INTERFACE(0x343DBF, 0xE4E4FF, 0xBCC2FF, 0xE1E1FE, 0x010055, 0x4147C9),
    REMINDERS(0x683E01, 0xFFDDB9, 0xFFB86C, 0xFFDDB9, 0x2B1700, 0x8A5100),
    DATABASE(0x0F4F42, 0xB7F0E1, 0x7FD8C0, 0xB7F0E1, 0x00201A, 0x006B58),
    TROUBLESHOOTING(0x94000D, 0xFFDAD6, 0xFFB4AB, 0xFEDAD6, 0x410002, 0xBA1A1A),
    LINKS(0x4A3C81, 0xECDDFF, 0xD0BCFF, 0xECDDFF, 0x21005D, 0x6B4EA8);

    @Composable
    fun badge() = color(if (isDarkScheme()) darkBadge else lightBadge)

    @Composable
    fun onBadge() = color(if (isDarkScheme()) darkOnBadge else lightOnBadge)

    @Composable
    fun title() = color(if (isDarkScheme()) darkTitle else lightTitle)

    private fun color(rgb: Long) = Color(0xFF000000 or rgb)
}

/** The icons of the rows that only a platform adds (the extended icons are not on the classpath of the app). */
object SettingsIcons {
    val WidgetOpacity: ImageVector get() = Icons.Outlined.WaterDrop
    val Customize: ImageVector get() = Icons.Outlined.Tune
    val ExportBackup: ImageVector get() = Icons.Outlined.FileUpload
    val ExportCsv: ImageVector get() = Icons.Outlined.Description
    val Import: ImageVector get() = Icons.Outlined.FileDownload
    val Folder: ImageVector get() = Icons.Outlined.Folder
    val BugReport: ImageVector get() = Icons.Outlined.BugReport
    val Repair: ImageVector get() = Icons.Outlined.Build
}

private val LocalSettingsTone = compositionLocalOf { SettingsTone.INTERFACE }

/** The fill of the rows of Settings: a tonal card on the background. */
@Composable
private fun settingsCardColor() = if (isDarkScheme()) Color(0xFF25242B) else Color(0xFFEDE9F1)

/** The top of Settings: the round back button and the large title under it. */
@Composable
fun SettingsHeader(onBack: () -> Unit) {
    val back = stringResource(Res.string.navigate_up)
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(closeButtonColor())
                .clickable(role = Role.Button, onClick = onBack)
                .testTag("settingsBack"),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = back, tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            stringResource(Res.string.settings),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp)
        )
    }
}

/**
 * A section of Settings: its [title] in the colour of [tone] and, under it, one card whose rows are 2 dp apart. The
 * card has the large corners of the design at its ends and the small ones of the rows between them.
 */
@Composable
fun SettingsGroup(title: String, tone: SettingsTone, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title,
        color = tone.title(),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 28.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
    )
    CompositionLocalProvider(LocalSettingsTone provides tone) {
        Column(
            Modifier.padding(horizontal = 12.dp).fillMaxWidth().clip(RoundedCornerShape(28.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content
        )
    }
}

/** A row of a [SettingsGroup]: a round badge with [icon], the title and the summary, and [trailing] at the end. */
@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    summary: String? = null,
    onClick: () -> Unit,
    role: Role? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val tone = LocalSettingsTone.current
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(settingsCardColor())
            .clickable(role = role, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(tone.badge()), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tone.onBadge(), modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f).padding(start = 16.dp, end = 12.dp)) {
            Text(title, color = scheme.onSurface, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
            if (!summary.isNullOrEmpty()) {
                Text(summary, color = scheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
        trailing?.invoke()
    }
}

/** A row that opens a link or a screen. */
@Composable
fun SettingsClickRow(icon: ImageVector, title: String, summary: String? = null, onClick: () -> Unit) =
    SettingsRow(icon, title, summary, onClick)

/** A row with an on/off switch, as the setting of a [SettingsSwitch]. */
@Composable
fun SettingsToggleRow(icon: ImageVector, title: String, summary: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val dark = isDarkScheme()
    SettingsRow(icon, title, summary, onClick = { onCheckedChange(!checked) }, role = Role.Switch) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            thumbContent = if (checked) {
                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else {
                null
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = if (dark) Color(0xFFC0C4FE) else Color(0xFF4147C9),
                checkedThumbColor = if (dark) Color(0xFF1F2799) else Color.White,
                checkedIconColor = if (dark) Color(0xFFC0C4FE) else Color(0xFF4147C9),
                uncheckedTrackColor = settingsCardColor(),
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

/** A row that asks for one of some [options] in a dialog. */
@Composable
fun SettingsChoiceRow(
    icon: ImageVector,
    title: String,
    summary: String?,
    options: List<ChoiceOption>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    SettingsRow(icon, title, summary, onClick = { open = true })
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(title) },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.RadioButton) {
                                    open = false
                                    onSelect(option.value)
                                }
                        ) {
                            RadioButton(selected = option.value == selected, onClick = null)
                            Text(option.label, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { open = false }) { Text(stringResource(Res.string.cancel)) } }
        )
    }
}
