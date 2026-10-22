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

import platform.Foundation.NSUserDefaults
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserDefaultsStorageTest {
    private val suite = "org.isoron.uhabits.test.UserDefaultsStorageTest"
    private lateinit var storage: UserDefaultsStorage

    @BeforeTest
    fun setUp() {
        storage = UserDefaultsStorage(NSUserDefaults(suiteName = suite), suite)
        storage.clear()
    }

    @AfterTest
    fun tearDown() {
        storage.clear()
    }

    @Test
    fun testDefaultsWhenMissing() {
        assertEquals(true, storage.getBoolean("missing", true))
        assertEquals(false, storage.getBoolean("missing", false))
        assertEquals(42, storage.getInt("missing", 42))
        assertEquals(42L, storage.getLong("missing", 42L))
        assertEquals("fallback", storage.getString("missing", "fallback"))
    }

    @Test
    fun testRoundTrip() {
        storage.putBoolean("bool", true)
        storage.putInt("int", -7)
        storage.putLong("long", 1_700_000_000_000L)
        storage.putString("string", "hello")
        assertTrue(storage.getBoolean("bool", false))
        assertEquals(-7, storage.getInt("int", 0))
        assertEquals(1_700_000_000_000L, storage.getLong("long", 0L))
        assertEquals("hello", storage.getString("string", ""))
    }

    @Test
    fun testStoredFalseAndZeroAreNotDefaults() {
        storage.putBoolean("bool", false)
        storage.putInt("int", 0)
        assertFalse(storage.getBoolean("bool", true))
        assertEquals(0, storage.getInt("int", 5))
    }

    @Test
    fun testRemoveAndClear() {
        storage.putInt("a", 1)
        storage.putInt("b", 2)
        storage.remove("a")
        assertEquals(9, storage.getInt("a", 9))
        assertEquals(2, storage.getInt("b", 9))
        storage.clear()
        assertEquals(9, storage.getInt("b", 9))
    }

    @Test
    fun testClearWithoutDomainName() {
        val withoutDomain = UserDefaultsStorage(NSUserDefaults(suiteName = suite), null)
        withoutDomain.putInt("a", 1)
        withoutDomain.putString("b", "x")
        withoutDomain.clear()
        assertEquals(-1, withoutDomain.getInt("a", -1))
        assertEquals("none", withoutDomain.getString("b", "none"))
    }

    @Test
    fun testClearWithDefaultConstructor() {
        // The test executable has no bundle identifier, so there is no domain name to remove
        val standard = UserDefaultsStorage()
        standard.putInt("org.isoron.uhabits.test.clear", 1)
        standard.clear()
        assertEquals(-1, standard.getInt("org.isoron.uhabits.test.clear", -1))
    }

    @Test
    fun testLongArray() {
        assertEquals(listOf(1L), storage.getLongArray("arr", longArrayOf(1L)).toList())
        storage.putLongArray("arr", longArrayOf(3L, 5L, 8L))
        assertEquals(listOf(3L, 5L, 8L), storage.getLongArray("arr", longArrayOf()).toList())
    }

    @Test
    fun testPreferencesPersistThroughStorage() {
        val first = Preferences(storage)
        assertFalse(first.isSkipEnabled)
        first.isSkipEnabled = true
        val second = Preferences(UserDefaultsStorage(NSUserDefaults(suiteName = suite), suite))
        assertTrue(second.isSkipEnabled)
    }
}
