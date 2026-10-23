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

package org.isoron.platform.notifications

import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.computeToday
import org.isoron.platform.time.setFirstWeekday
import org.isoron.platform.time.setToday
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.reminders.ReminderScheduler
import org.isoron.uhabits.core.utils.MidnightTimer

/**
 * What Android does in `HabitsApplication.onCreate` and `ListHabitsActivity.onResume/onPause`, for
 * an iOS app that has no background timers and can stay suspended across midnight: on launch and
 * every time the app returns from the background it re-checks the day, the first weekday and the
 * reminders. Pending reminders are cleared first, so those of habits that lost theirs disappear
 * and the scheduler's count of pending requests starts from zero.
 *
 * The app calls these from its lifecycle, or through [AppLifecycleObserver].
 */
class AppForegroundRefresher(
    private val preferences: Preferences,
    private val midnightTimer: MidnightTimer,
    private val reminderScheduler: ReminderScheduler,
    private val systemScheduler: AppleSystemScheduler,
    private val todayProvider: () -> LocalDate = { computeToday(preferences.midnightDelayHours, 0) }
) {
    private var launched = false
    private var wasInBackground = false

    /**
     * Sets up the day, the first weekday, the timer and the reminders. Call it once when the app starts.
     */
    fun onLaunch() {
        launched = true
        wasInBackground = false
        setToday(todayProvider())
        setFirstWeekday(preferences.firstWeekday)
        midnightTimer.onResume()
        rescheduleAll()
    }

    /**
     * The app became active. The first activation after [onLaunch] only checks the day; after the
     * app was in the background (or a new day started) the reminders are scheduled again too.
     * Launches first if [onLaunch] has not run.
     */
    fun onForeground() {
        if (!launched) {
            onLaunch()
            return
        }
        setFirstWeekday(preferences.firstWeekday)
        val dayChanged = midnightTimer.notifyIfDayChanged()
        if (wasInBackground || dayChanged) {
            wasInBackground = false
            midnightTimer.onResume()
            rescheduleAll()
        }
    }

    fun onBackground() {
        wasInBackground = true
        midnightTimer.onPause()
    }

    private fun rescheduleAll() {
        systemScheduler.clear()
        reminderScheduler.scheduleAll()
    }
}
