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

package io.github.crisacm.salem.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.AppScope
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.inject.AppContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class SharedPreferencesStorage(
    @AppContext context: Context
) : SharedPreferences.OnSharedPreferenceChangeListener, Preferences.Storage {

    private val sharedPrefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private var preferences: Preferences? = null
    private val echo = EchoFilter()

    init {
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false)
    }

    override fun clear() {
        echo.forgetAll()
        sharedPrefs.edit().clear().apply()
    }

    override fun getBoolean(key: String, defValue: Boolean) =
        sharedPrefs.getBoolean(key, defValue)

    override fun getInt(key: String, defValue: Int) =
        sharedPrefs.getInt(key, defValue)

    override fun getLong(key: String, defValue: Long) =
        sharedPrefs.getLong(key, defValue)

    override fun getString(key: String, defValue: String): String =
        sharedPrefs.getString(key, defValue)!!

    override fun onAttached(preferences: Preferences) {
        this.preferences = preferences
    }

    override fun putBoolean(key: String, value: Boolean) {
        echo.written(key, value)
        sharedPrefs.edit().putBoolean(key, value).apply()
    }

    override fun putInt(key: String, value: Int) =
        sharedPrefs.edit().putInt(key, value).apply()

    override fun putLong(key: String, value: Long) =
        sharedPrefs.edit().putLong(key, value).apply()

    override fun putString(key: String, value: String) =
        sharedPrefs.edit().putString(key, value).apply()

    override fun remove(key: String) {
        echo.forget(key)
        sharedPrefs.edit().remove(key).apply()
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?
    ) {
        val preferences = this.preferences ?: return
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        // The setters of Preferences write through putBoolean and have already told their listeners
        when (key) {
            "pref_checkmark_reverse_order" ->
                if (echo.isNew(key, getBoolean(key, false))) {
                    preferences.isCheckmarkSequenceReversed = getBoolean(key, false)
                }
            "pref_midnight_delay" ->
                if (echo.isNew(key, getBoolean(key, false))) {
                    preferences.isMidnightDelayEnabled = getBoolean(key, false)
                }
            "pref_sticky_notifications" ->
                if (echo.isNew(key, getBoolean(key, false))) {
                    preferences.setNotificationsSticky(getBoolean(key, false))
                }
            "pref_unknown_enabled" ->
                if (echo.isNew(key, getBoolean(key, false))) {
                    preferences.areQuestionMarksEnabled = getBoolean(key, false)
                }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }
}
