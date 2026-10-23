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

package io.github.crisacm.salem.activities.settings

import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.habits.list.RESULT_BUG_REPORT
import io.github.crisacm.salem.activities.habits.list.RESULT_EXPORT_CSV
import io.github.crisacm.salem.activities.habits.list.RESULT_EXPORT_DB
import io.github.crisacm.salem.activities.habits.list.RESULT_IMPORT_DATA
import io.github.crisacm.salem.activities.habits.list.RESULT_REPAIR_DB
import io.github.crisacm.salem.ui.compose.CategoryHeader
import io.github.crisacm.salem.ui.compose.ChoiceOption
import io.github.crisacm.salem.ui.compose.ChoiceRow
import io.github.crisacm.salem.ui.compose.ClickRow
import io.github.crisacm.salem.ui.compose.SalemTopAppBar
import io.github.crisacm.salem.ui.compose.SwitchRow
import io.github.crisacm.salem.ui.compose.rememberDefaultPreferences
import io.github.crisacm.salem.ui.compose.rememberPrefBoolean
import io.github.crisacm.salem.ui.compose.rememberPrefString

const val PREF_FIRST_WEEKDAY = "pref_first_weekday"
const val PREF_WIDGET_OPACITY = "pref_widget_opacity"
const val PREF_PUBLIC_BACKUP_FOLDER = "publicBackupFolder"

class SettingsActions(
    val onBack: () -> Unit,
    /** Finishes the screen with one of the `RESULT_*` codes handled by `ListHabitsScreen`. */
    val onResult: (Int) -> Unit,
    val onCustomizeNotification: () -> Unit,
    val onPickBackupFolder: () -> Unit,
    val onHelp: () -> Unit,
    val onRate: () -> Unit,
    val onAbout: () -> Unit
)

@Composable
fun SettingsContent(
    weekdayNames: List<String>,
    defaultFirstWeekday: Int,
    showDeveloper: Boolean,
    actions: SettingsActions
) {
    val prefs = rememberDefaultPreferences()
    Scaffold(
        topBar = { SalemTopAppBar(stringResource(R.string.settings), actions.onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            InterfaceSection(prefs, weekdayNames, defaultFirstWeekday)
            ReminderSection(prefs, actions)
            DatabaseSection(prefs, actions)
            CategoryHeader(stringResource(R.string.troubleshooting))
            ClickRow(stringResource(R.string.generate_bug_report)) { actions.onResult(RESULT_BUG_REPORT) }
            ClickRow(stringResource(R.string.repair_database)) { actions.onResult(RESULT_REPAIR_DB) }
            CategoryHeader(stringResource(R.string.links))
            ClickRow(stringResource(R.string.help), onClick = actions.onHelp)
            ClickRow(stringResource(R.string.pref_rate_this_app), onClick = actions.onRate)
            ClickRow(stringResource(R.string.about), onClick = actions.onAbout)
            if (showDeveloper) {
                CategoryHeader(stringResource(R.string.development))
                PrefSwitch(prefs, "pref_developer", R.string.enable_developer_mode, null)
            }
        }
    }
}

@Composable
private fun PrefSwitch(prefs: SharedPreferences, key: String, title: Int, summary: Int?) {
    val checked by rememberPrefBoolean(prefs, key)
    SwitchRow(
        title = stringResource(title),
        summary = summary?.let { stringResource(it) },
        checked = checked,
        onCheckedChange = { prefs.edit().putBoolean(key, it).apply() }
    )
}

@Composable
private fun InterfaceSection(prefs: SharedPreferences, weekdayNames: List<String>, defaultFirstWeekday: Int) {
    CategoryHeader(stringResource(R.string.interface_preferences))
    PrefSwitch(prefs, "pref_short_toggle", R.string.pref_toggle_title, R.string.pref_toggle_description_2)
    PrefSwitch(prefs, "pref_midnight_delay", R.string.pref_midnight_delay_title, R.string.pref_midnight_delay_description)
    PrefSwitch(prefs, "pref_skip_enabled", R.string.pref_skip_title, R.string.pref_skip_description)
    PrefSwitch(prefs, "pref_unknown_enabled", R.string.pref_unknown_title, R.string.pref_unknown_description)
    PrefSwitch(prefs, "pref_checkmark_reverse_order", R.string.reverse_days, R.string.reverse_days_description)
    PrefSwitch(prefs, "pref_pure_black", R.string.use_pure_black, R.string.pure_black_description)
    PrefSwitch(prefs, "pref_disable_animation", R.string.pref_animations_title, R.string.pref_animations_description)

    val opacity by rememberPrefString(prefs, PREF_WIDGET_OPACITY, "255")
    val opacityLabels = stringArrayResource(R.array.widget_opacity_entries)
    val opacityValues = stringArrayResource(R.array.widget_opacity_values)
    ChoiceRow(
        title = stringResource(R.string.widget_opacity_title),
        summary = stringResource(R.string.widget_opacity_description),
        options = opacityValues.zip(opacityLabels) { value, label -> ChoiceOption(value, label) },
        selected = opacity,
        onSelect = { prefs.edit().putString(PREF_WIDGET_OPACITY, it).apply() }
    )

    val storedWeekday by rememberPrefString(prefs, PREF_FIRST_WEEKDAY)
    val currentWeekday = storedWeekday?.toIntOrNull()?.takeIf { it in 1..7 } ?: defaultFirstWeekday
    ChoiceRow(
        title = stringResource(R.string.first_day_of_the_week),
        summary = firstWeekdaySummary(weekdayNames, currentWeekday),
        options = firstWeekdayOptions(weekdayNames).map { ChoiceOption(it.value, it.name) },
        selected = currentWeekday.toString(),
        onSelect = { prefs.edit().putString(PREF_FIRST_WEEKDAY, it).apply() }
    )
}

@Composable
private fun ReminderSection(prefs: SharedPreferences, actions: SettingsActions) {
    CategoryHeader(stringResource(R.string.reminder))
    PrefSwitch(prefs, "pref_sticky_notifications", R.string.sticky_notifications, R.string.sticky_notifications_description)
    ClickRow(
        stringResource(R.string.customize_notification),
        stringResource(R.string.customize_notification_summary),
        actions.onCustomizeNotification
    )
}

@Composable
private fun DatabaseSection(prefs: SharedPreferences, actions: SettingsActions) {
    CategoryHeader(stringResource(R.string.database))
    ClickRow(
        stringResource(R.string.export_full_backup),
        stringResource(R.string.export_full_backup_summary)
    ) { actions.onResult(RESULT_EXPORT_DB) }
    ClickRow(
        stringResource(R.string.export_to_csv),
        stringResource(R.string.export_as_csv_summary)
    ) { actions.onResult(RESULT_EXPORT_CSV) }
    ClickRow(
        stringResource(R.string.import_data),
        stringResource(R.string.import_data_summary)
    ) { actions.onResult(RESULT_IMPORT_DATA) }
    val folder by rememberPrefString(prefs, PREF_PUBLIC_BACKUP_FOLDER)
    ClickRow(
        stringResource(R.string.select_public_backup_folder),
        folder?.let { publicFolderDescription(it) } ?: stringResource(R.string.no_public_backup_folder_selected),
        actions.onPickBackupFolder
    )
}

private fun publicFolderDescription(uriString: String): String = runCatching {
    val uri = Uri.parse(uriString)
    when (uri.scheme) {
        "content" -> treeDocumentPath(
            DocumentsContract.getTreeDocumentId(uri),
            Environment.getExternalStorageDirectory().absolutePath
        )
        "file" -> java.io.File(uri.path!!).absolutePath
        else -> null
    }
}.getOrNull() ?: uriString
