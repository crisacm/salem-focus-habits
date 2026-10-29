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

import android.app.backup.BackupManager
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.AndroidThemeSwitcher
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.NotificationTray
import io.github.crisacm.salem.notifications.AndroidNotificationTray.Companion.createAndroidNotificationChannel
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.ui.compose.SalemTheme
import io.github.crisacm.salem.ui.settings.SettingsContent
import io.github.crisacm.salem.ui.settings.SettingsState
import io.github.crisacm.salem.ui.settings.weekdayNamesFromSaturday
import io.github.crisacm.salem.utils.applyAppLanguage
import io.github.crisacm.salem.utils.startActivitySafely
import io.github.crisacm.salem.widgets.WidgetUpdater
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: Preferences
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var widgetUpdater: WidgetUpdater
    private lateinit var state: SettingsState

    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == PREF_WIDGET_OPACITY) widgetUpdater.updateWidgets()
        BackupManager.dataChanged(packageName)
    }

    private val backupFolderPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data ?: return@registerForActivityResult
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flags)
            sharedPrefs.edit().putString(PREF_PUBLIC_BACKUP_FOLDER, uri.toString()).apply()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as HabitsApplication).component
        prefs = component.preferences
        widgetUpdater = component.widgetUpdater
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themeSwitcher = AndroidThemeSwitcher(this, prefs)
        themeSwitcher.apply()

        val weekdayNames = weekdayNamesFromSaturday(JavaLocalDateFormatter(Locale.getDefault())::longWeekdayName)
        state = SettingsState(prefs)
        val androidActions = AndroidSettingsActions(
            onResult = {
                setResult(it)
                finish()
            },
            onCustomizeNotification = ::showNotificationSettings,
            onPickBackupFolder = ::pickBackupFolder
        )
        setContent {
            SalemTheme(themeSwitcher.currentTheme) {
                SettingsContent(
                    state = state,
                    weekdayNames = weekdayNames,
                    showAnimationsSwitch = true,
                    onBack = { finish() },
                    onHelp = { openUrl(R.string.helpURL) },
                    onRate = { openUrl(R.string.playStoreURL) },
                    onAbout = { startActivity(component.intentFactory.startAboutActivity(this)) },
                    onThemeChanged = { recreate() },
                    onLanguageChanged = ::applyAppLanguage,
                    interfaceExtras = { WidgetOpacityRow(sharedPrefs) },
                    middleSections = { AndroidSettingsSections(state, sharedPrefs, androidActions) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPrefs.registerOnSharedPreferenceChangeListener(changeListener)
        // The developer mode may have been turned on in About
        state.refresh()
    }

    override fun onPause() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(changeListener)
        super.onPause()
    }

    private fun openUrl(url: Int) =
        startActivitySafely(Intent(Intent.ACTION_VIEW, Uri.parse(getString(url))))

    private fun showNotificationSettings() {
        createAndroidNotificationChannel(this)
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationTray.REMINDERS_CHANNEL_ID)
        startActivity(intent)
    }

    private fun pickBackupFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        backupFolderPicker.launch(intent)
    }
}
