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

import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.intents.IntentFactory
import io.github.crisacm.salem.utils.showMessage
import io.github.crisacm.salem.utils.startActivitySafely

class AboutScreen(
    private val activity: AboutActivity,
    private val intents: IntentFactory,
    private val prefs: Preferences
) {

    private val developerCountdown = DeveloperCountdown()

    fun showRateAppWebsite() =
        activity.startActivitySafely(intents.rateApp(activity))

    fun showSendFeedbackScreen() =
        activity.startActivitySafely(intents.sendFeedback(activity))

    fun showSourceCodeWebsite() =
        activity.startActivitySafely(intents.viewSourceCode(activity))

    fun showTranslationWebsite() =
        activity.startActivitySafely(intents.helpTranslate(activity))

    fun showPrivacyPolicyWebsite() =
        activity.startActivitySafely(intents.privacyPolicy(activity))

    fun showCodeContributorsWebsite() =
        activity.startActivitySafely(intents.codeContributors(activity))

    fun onPressDeveloperCountdown() {
        if (developerCountdown.onTap()) {
            prefs.isDeveloper = true
            activity.showMessage(activity.resources.getString(R.string.you_are_now_a_developer))
        }
    }
}
