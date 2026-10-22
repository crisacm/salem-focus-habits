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

package org.isoron.uhabits.core.preferences

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

/**
 * [Preferences.Storage] backed by `NSUserDefaults`.
 *
 * `NSUserDefaults` returns 0 or false for missing keys, so every getter checks that the key exists
 * to honour the default value, like `SharedPreferences` does on Android.
 *
 * @param defaults the defaults database to use
 * @param domainName the persistent domain that [clear] removes. For the standard defaults this is the
 *   bundle identifier; for a suite created with `NSUserDefaults(suiteName)` it is the suite name. When it
 *   is null (for example in a test executable, which has no bundle identifier) [clear] removes every key
 *   of [defaults] instead.
 *
 * Integers are stored as `NSInteger` (64 bits), so [getInt] truncates a value that was stored with
 * [putLong] and does not fit in an `Int`. `SharedPreferences` would throw instead.
 */
class UserDefaultsStorage(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val domainName: String? = NSBundle.mainBundle.bundleIdentifier
) : Preferences.Storage {

    override fun clear() {
        if (domainName != null) {
            NSUserDefaults.standardUserDefaults.removePersistentDomainForName(domainName)
        } else {
            for (key in defaults.dictionaryRepresentation().keys) defaults.removeObjectForKey(key as String)
        }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        if (contains(key)) defaults.boolForKey(key) else defValue

    override fun getInt(key: String, defValue: Int): Int =
        if (contains(key)) defaults.integerForKey(key).toInt() else defValue

    override fun getLong(key: String, defValue: Long): Long =
        if (contains(key)) defaults.integerForKey(key) else defValue

    override fun getString(key: String, defValue: String): String =
        defaults.stringForKey(key) ?: defValue

    override fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, forKey = key)
    }

    override fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = key)
    }

    override fun putLong(key: String, value: Long) {
        defaults.setInteger(value, forKey = key)
    }

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }

    private fun contains(key: String): Boolean = defaults.objectForKey(key) != null
}
