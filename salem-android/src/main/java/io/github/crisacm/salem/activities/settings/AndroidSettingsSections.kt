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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.habits.list.RESULT_BUG_REPORT
import io.github.crisacm.salem.activities.habits.list.RESULT_EXPORT_CSV
import io.github.crisacm.salem.activities.habits.list.RESULT_EXPORT_DB
import io.github.crisacm.salem.activities.habits.list.RESULT_IMPORT_DATA
import io.github.crisacm.salem.activities.habits.list.RESULT_REPAIR_DB
import io.github.crisacm.salem.ui.compose.ChoiceOption
import io.github.crisacm.salem.ui.compose.rememberPrefString
import io.github.crisacm.salem.ui.settings.SettingsChoiceRow
import io.github.crisacm.salem.ui.settings.SettingsClickRow
import io.github.crisacm.salem.ui.settings.SettingsGroup
import io.github.crisacm.salem.ui.settings.SettingsIcons
import io.github.crisacm.salem.ui.settings.SettingsState
import io.github.crisacm.salem.ui.settings.SettingsSwitch
import io.github.crisacm.salem.ui.settings.SettingsSwitchRow
import io.github.crisacm.salem.ui.settings.SettingsTone

const val PREF_WIDGET_OPACITY = "pref_widget_opacity"
const val PREF_PUBLIC_BACKUP_FOLDER = "publicBackupFolder"

/** What the Android-only rows of Settings ask the activity to do. */
class AndroidSettingsActions(
    /** Finishes the screen with one of the `RESULT_*` codes handled by `ListHabitsScreen`. */
    val onResult: (Int) -> Unit,
    val onCustomizeNotification: () -> Unit,
    val onPickBackupFolder: () -> Unit
)

/** The widget opacity, which only the Android widgets use. It goes after the shared switches of the interface. */
@Composable
fun WidgetOpacityRow(sharedPrefs: SharedPreferences) {
    val opacity by rememberPrefString(sharedPrefs, PREF_WIDGET_OPACITY, "255")
    val opacityLabels = stringArrayResource(R.array.widget_opacity_entries)
    val opacityValues = stringArrayResource(R.array.widget_opacity_values)
    SettingsChoiceRow(
        icon = SettingsIcons.WidgetOpacity,
        title = stringResource(R.string.widget_opacity_title),
        summary = stringResource(R.string.widget_opacity_description),
        options = opacityValues.zip(opacityLabels) { value, label -> ChoiceOption(value, label) },
        selected = opacity,
        onSelect = { sharedPrefs.edit().putString(PREF_WIDGET_OPACITY, it).apply() }
    )
}

/** The reminder, database and troubleshooting sections: notifications, backups and the files of the device. */
@Composable
fun AndroidSettingsSections(state: SettingsState, sharedPrefs: SharedPreferences, actions: AndroidSettingsActions) {
    SettingsGroup(stringResource(R.string.reminders), SettingsTone.REMINDERS) {
        SettingsSwitchRow(state, SettingsSwitch.STICKY_NOTIFICATIONS)
        SettingsClickRow(
            SettingsIcons.Customize,
            stringResource(R.string.customize_notification),
            stringResource(R.string.customize_notification_summary),
            actions.onCustomizeNotification
        )
    }

    SettingsGroup(stringResource(R.string.database), SettingsTone.DATABASE) {
        SettingsClickRow(
            SettingsIcons.ExportBackup,
            stringResource(R.string.export_full_backup),
            stringResource(R.string.export_full_backup_summary)
        ) { actions.onResult(RESULT_EXPORT_DB) }
        SettingsClickRow(
            SettingsIcons.ExportCsv,
            stringResource(R.string.export_to_csv),
            stringResource(R.string.export_as_csv_summary)
        ) { actions.onResult(RESULT_EXPORT_CSV) }
        SettingsClickRow(
            SettingsIcons.Import,
            stringResource(R.string.import_data),
            stringResource(R.string.import_data_summary)
        ) { actions.onResult(RESULT_IMPORT_DATA) }
        val folder by rememberPrefString(sharedPrefs, PREF_PUBLIC_BACKUP_FOLDER)
        SettingsClickRow(
            SettingsIcons.Folder,
            stringResource(R.string.select_public_backup_folder),
            folder?.let { publicFolderDescription(it) } ?: stringResource(R.string.no_public_backup_folder_selected),
            actions.onPickBackupFolder
        )
    }

    SettingsGroup(stringResource(R.string.troubleshooting), SettingsTone.TROUBLESHOOTING) {
        SettingsClickRow(SettingsIcons.BugReport, stringResource(R.string.generate_bug_report)) { actions.onResult(RESULT_BUG_REPORT) }
        SettingsClickRow(SettingsIcons.Repair, stringResource(R.string.repair_database)) { actions.onResult(RESULT_REPAIR_DB) }
    }
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
