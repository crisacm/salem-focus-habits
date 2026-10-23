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

package io.github.crisacm.salem.core.io

import kotlinx.coroutines.test.runTest
import io.github.crisacm.salem.platform.io.ZipReader
import io.github.crisacm.salem.platform.io.formatInvariant
import io.github.crisacm.salem.core.BaseUnitTest
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Data written by the app (CSV files, migration file names) must not depend on the device locale.
 * Arabic and Persian locales use non-Latin digits and decimal separators.
 */
class LocaleIndependenceTest : BaseUnitTest() {
    private val locales = listOf("es-ES", "de-DE", "ar-EG", "fa-IR")
    private lateinit var original: Locale

    @BeforeTest
    override fun setUp() {
        super.setUp()
        original = Locale.getDefault()
        habitList.add(fixtures.createShortHabit())
        habitList.add(fixtures.createNumericalHabit())
    }

    @AfterTest
    fun restoreLocale() {
        Locale.setDefault(original)
    }

    @Test
    fun testFormatInvariantIgnoresLocale() {
        for (tag in locales) {
            Locale.setDefault(Locale.forLanguageTag(tag))
            assertEquals("09.sql", formatInvariant("%02d.sql", 9), tag)
            assertEquals("007", formatInvariant("%03d", 7), tag)
            assertEquals("0.5000", formatInvariant("%.4f", 0.5), tag)
        }
    }

    @Test
    fun testCsvExportIgnoresLocale() = runTest {
        for (tag in locales) {
            Locale.setDefault(Locale.forLanguageTag(tag))
            val exporter = HabitsCSVExporter(habitList, habitList.toList())
            val entries = ZipReader(exporter.writeArchive()).entries()
            for (entry in entries) {
                assertTrue(entry.content.all { it.code < 128 }, "${entry.name} has non-ASCII characters ($tag)")
            }
            val scores = entries.first { it.name == "Scores.csv" }.content.lines().drop(1).filter { it.isNotBlank() }
            assertTrue(scores.isNotEmpty())
            val row = Regex("^\\d{4}-\\d{2}-\\d{2}(,\\d+\\.\\d{4})+,?$")
            for (line in scores) assertTrue(row.matches(line), "bad score row '$line' ($tag)")
            val habits = habitList.writeCSV()
            assertTrue(habits.lines().any { it.contains(",AT_LEAST,2.0,") || it.contains(",AT_MOST,2.0,") }, "target value ($tag)")
        }
    }
}
