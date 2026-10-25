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

import java.util.concurrent.ConcurrentHashMap

/**
 * Tells a change that the app wrote itself from one made behind its back. [SharedPreferencesStorage] writes a value
 * and SharedPreferences then reports it again; a `Preferences` setter has already told its listeners about it, so
 * that second report must not call the setter again.
 */
class EchoFilter {
    private val known = ConcurrentHashMap<String, Boolean>()

    /** The app wrote [value] for [key]. */
    fun written(key: String, value: Boolean) {
        known[key] = value
    }

    /** True when [value] is not the last one known for [key] (and from now on it is). */
    fun isNew(key: String, value: Boolean): Boolean = known.put(key, value) != value

    fun forget(key: String) {
        known.remove(key)
    }

    fun forgetAll() = known.clear()
}
