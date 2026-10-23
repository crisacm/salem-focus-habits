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

package io.github.crisacm.salem.activities.about

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.github.crisacm.salem.BuildConfig
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.AndroidThemeSwitcher
import io.github.crisacm.salem.ui.compose.SalemTheme

/**
 * Activity that allows the user to see information about the app itself.
 * Display current version, link to Google Play and list of contributors.
 */
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as HabitsApplication
        val screen = AboutScreen(
            this,
            app.component.intentFactory,
            app.component.preferences
        )
        val themeSwitcher = AndroidThemeSwitcher(this, app.component.preferences)
        themeSwitcher.apply()
        val version = getString(R.string.version_n, BuildConfig.VERSION_NAME)
        val actions = AboutActions(
            onBack = { finish() },
            onVersionTap = screen::onPressDeveloperCountdown,
            onRate = screen::showRateAppWebsite,
            onFeedback = screen::showSendFeedbackScreen,
            onTranslate = screen::showTranslationWebsite,
            onSource = screen::showSourceCodeWebsite,
            onPrivacy = screen::showPrivacyPolicyWebsite,
            onContributors = screen::showCodeContributorsWebsite
        )
        setContent {
            SalemTheme(themeSwitcher.currentTheme) { AboutContent(version, actions) }
        }
    }
}
