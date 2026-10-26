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

package io.github.crisacm.salem.platform.io

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults

/**
 * The App Group shared by the app and its extensions. Without the entitlement (a build without signing, or a test) the
 * system gives no container, and the callers keep to the private places of the app.
 */
object AppGroup {
    /** The folder shared with the extensions, or null when this build has no access to the group. */
    @OptIn(ExperimentalForeignApi::class)
    fun containerPath(groupId: String): String? =
        NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(groupId)?.path

    /** The defaults shared with the extensions when the group is available, the standard ones otherwise. */
    fun userDefaults(groupId: String): NSUserDefaults =
        if (containerPath(groupId) != null) {
            NSUserDefaults(suiteName = groupId)
        } else {
            NSUserDefaults.standardUserDefaults
        }
}
