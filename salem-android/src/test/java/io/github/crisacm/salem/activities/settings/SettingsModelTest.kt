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

package io.github.crisacm.salem.activities.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsModelTest {
    private val names = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    @Test
    fun weekdayOptionsStartAtSaturdayWithSundayBasedValues() {
        val options = firstWeekdayOptions(names)
        assertEquals(listOf("7", "1", "2", "3", "4", "5", "6"), options.map { it.value })
        assertEquals(names, options.map { it.name })
    }

    @Test
    fun weekdaySummaryUsesTheSundayBasedNumber() {
        assertEquals("Sunday", firstWeekdaySummary(names, 1))
        assertEquals("Monday", firstWeekdaySummary(names, 2))
        assertEquals("Saturday", firstWeekdaySummary(names, 7))
    }

    @Test
    fun treeDocumentPathOnPrimaryStorage() {
        assertEquals("/sdcard/Backups/Salem", treeDocumentPath("primary:Backups/Salem", "/sdcard"))
        assertEquals("/sdcard", treeDocumentPath("primary:", "/sdcard"))
        assertEquals("/sdcard", treeDocumentPath("PRIMARY:", "/sdcard"))
    }

    @Test
    fun treeDocumentPathOnRemovableStorage() {
        assertEquals("/storage/1234-ABCD/Docs", treeDocumentPath("1234-ABCD:Docs", "/sdcard"))
        assertEquals("/storage/1234-ABCD", treeDocumentPath("1234-ABCD:", "/sdcard"))
    }

    @Test
    fun treeDocumentPathWithoutSeparatorOrId() {
        assertEquals("/storage/1234-ABCD", treeDocumentPath("1234-ABCD", "/sdcard"))
        assertEquals("/storage/", treeDocumentPath("", "/sdcard"))
    }

    @Test
    fun treeDocumentPathKeepsColonsInTheRelativePart() {
        assertEquals("/sdcard/a:b", treeDocumentPath("primary:a:b", "/sdcard"))
    }

    @Test
    fun weekdayOptionsAreTruncatedWhenNamesAreMissing() {
        assertEquals(2, firstWeekdayOptions(names.take(2)).size)
    }
}
