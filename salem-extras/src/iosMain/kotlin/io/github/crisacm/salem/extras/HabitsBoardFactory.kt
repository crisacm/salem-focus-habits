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
package io.github.crisacm.salem.extras

import io.github.crisacm.salem.core.APP_GROUP_ID
import io.github.crisacm.salem.core.DATABASE_FILENAME
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.preferences.UserDefaultsStorage
import io.github.crisacm.salem.platform.io.AppGroup
import io.github.crisacm.salem.platform.io.AppleFileOpener
import io.github.crisacm.salem.platform.io.SqliterDatabaseOpener
import io.github.crisacm.salem.platform.notifications.UNDeliveredNotificationCenter
import io.github.crisacm.salem.platform.time.computeToday
import platform.Foundation.NSFileManager

/**
 * Opens the database of the app, from the folder it shares with its extensions, for the widget and the shortcuts. The
 * app creates and migrates the database; an extension only uses it. Called from Swift as `HabitsBoardFactory.shared`.
 */
object HabitsBoardFactory {
    /** The board of the habits of the app, or null when the app has not created its database (or this build cannot reach the App Group). */
    fun create(): HabitsBoard? {
        val path = AppleFileOpener.createDefault(APP_GROUP_ID).openUserFile(DATABASE_FILENAME).pathString
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
        val preferences = Preferences(UserDefaultsStorage(AppGroup.userDefaults(APP_GROUP_ID)))
        return HabitsBoard(
            database = SqliterDatabaseOpener().open(path),
            date = { computeToday(preferences.midnightDelayHours, 0) },
            firstWeekday = preferences.firstWeekday,
            isSkipEnabled = preferences.isSkipEnabled,
            areQuestionMarksEnabled = preferences.areQuestionMarksEnabled,
            // A delivered reminder of a habit that was just changed is no longer news
            onChanged = { habitId -> UNDeliveredNotificationCenter().removeDeliveredOfHabit(habitId) }
        )
    }
}
