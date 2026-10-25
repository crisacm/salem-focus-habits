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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.enable_developer_mode
import io.github.crisacm.salem.ui.resources.pref_animations_description
import io.github.crisacm.salem.ui.resources.pref_animations_title
import io.github.crisacm.salem.ui.resources.pref_midnight_delay_description
import io.github.crisacm.salem.ui.resources.pref_midnight_delay_title
import io.github.crisacm.salem.ui.resources.pref_skip_description
import io.github.crisacm.salem.ui.resources.pref_skip_title
import io.github.crisacm.salem.ui.resources.pref_toggle_description_2
import io.github.crisacm.salem.ui.resources.pref_toggle_title
import io.github.crisacm.salem.ui.resources.pref_unknown_description
import io.github.crisacm.salem.ui.resources.pref_unknown_title
import io.github.crisacm.salem.ui.resources.pure_black_description
import io.github.crisacm.salem.ui.resources.reverse_days
import io.github.crisacm.salem.ui.resources.reverse_days_description
import io.github.crisacm.salem.ui.resources.sticky_notifications
import io.github.crisacm.salem.ui.resources.sticky_notifications_description
import io.github.crisacm.salem.ui.resources.use_pure_black
import org.jetbrains.compose.resources.StringResource

/** The on/off settings, with the texts of their rows. */
enum class SettingsSwitch(val title: StringResource, val summary: StringResource?) {
    SHORT_TOGGLE(Res.string.pref_toggle_title, Res.string.pref_toggle_description_2),
    MIDNIGHT_DELAY(Res.string.pref_midnight_delay_title, Res.string.pref_midnight_delay_description),
    SKIP(Res.string.pref_skip_title, Res.string.pref_skip_description),
    UNKNOWN(Res.string.pref_unknown_title, Res.string.pref_unknown_description),
    REVERSE_DAYS(Res.string.reverse_days, Res.string.reverse_days_description),
    PURE_BLACK(Res.string.use_pure_black, Res.string.pure_black_description),
    DISABLE_ANIMATION(Res.string.pref_animations_title, Res.string.pref_animations_description),
    STICKY_NOTIFICATIONS(Res.string.sticky_notifications, Res.string.sticky_notifications_description),
    DEVELOPER(Res.string.enable_developer_mode, null)
}

/**
 * What the Settings screen shows and changes. Every change goes through the setters of [preferences] (so its
 * listeners hear it, whatever the platform storage does) and then calls [onChanged], which a platform uses to apply
 * the setting to what is already on screen (iOS does; Android reads its settings again when it opens each screen).
 */
class SettingsState(private val preferences: Preferences, private val onChanged: () -> Unit = {}) {
    private val switches = mutableStateMapOf<SettingsSwitch, Boolean>()

    var firstWeekday by mutableStateOf(preferences.firstWeekday)
        private set

    init {
        refresh()
    }

    /** Reads everything again, for what other screens may have changed since (the developer mode, for one). */
    fun refresh() {
        SettingsSwitch.entries.forEach { switches[it] = read(it) }
        firstWeekday = preferences.firstWeekday
    }

    fun isOn(switch: SettingsSwitch): Boolean = switches.getValue(switch)

    fun set(switch: SettingsSwitch, value: Boolean) {
        write(switch, value)
        switches[switch] = value
        onChanged()
    }

    fun selectFirstWeekday(day: DayOfWeek) {
        preferences.firstWeekday = day
        firstWeekday = day
        onChanged()
    }

    private fun read(switch: SettingsSwitch) = when (switch) {
        SettingsSwitch.SHORT_TOGGLE -> preferences.isShortToggleEnabled
        SettingsSwitch.MIDNIGHT_DELAY -> preferences.isMidnightDelayEnabled
        SettingsSwitch.SKIP -> preferences.isSkipEnabled
        SettingsSwitch.UNKNOWN -> preferences.areQuestionMarksEnabled
        SettingsSwitch.REVERSE_DAYS -> preferences.isCheckmarkSequenceReversed
        SettingsSwitch.PURE_BLACK -> preferences.isPureBlackEnabled
        SettingsSwitch.DISABLE_ANIMATION -> preferences.isConfettiAnimationDisabled
        SettingsSwitch.STICKY_NOTIFICATIONS -> preferences.shouldMakeNotificationsSticky()
        SettingsSwitch.DEVELOPER -> preferences.isDeveloper
    }

    private fun write(switch: SettingsSwitch, value: Boolean) = when (switch) {
        SettingsSwitch.SHORT_TOGGLE -> preferences.isShortToggleEnabled = value
        SettingsSwitch.MIDNIGHT_DELAY -> preferences.isMidnightDelayEnabled = value
        SettingsSwitch.SKIP -> preferences.isSkipEnabled = value
        SettingsSwitch.UNKNOWN -> preferences.areQuestionMarksEnabled = value
        SettingsSwitch.REVERSE_DAYS -> preferences.isCheckmarkSequenceReversed = value
        SettingsSwitch.PURE_BLACK -> preferences.isPureBlackEnabled = value
        SettingsSwitch.DISABLE_ANIMATION -> preferences.isConfettiAnimationDisabled = value
        SettingsSwitch.STICKY_NOTIFICATIONS -> preferences.setNotificationsSticky(value)
        SettingsSwitch.DEVELOPER -> preferences.isDeveloper = value
    }
}
