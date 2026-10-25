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

package io.github.crisacm.salem.ui

import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.add_habit
import io.github.crisacm.salem.ui.resources.no_habits_left_to_do
import io.github.crisacm.salem.ui.resources.toast_habits_deleted
import io.github.crisacm.salem.ui.resources.x_times_per_y_days
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** The strings of salem-android, copied into the Compose resources of this module, resolve like the Android ones. */
class SharedStringsTest {
    private val original = Locale.getDefault()

    @AfterTest
    fun restoreLocale() = Locale.setDefault(original)

    private fun use(language: String, country: String = "") = Locale.setDefault(Locale(language, country))

    @Test
    fun resolvesEnglishStringsWithEscapedApostrophes() = runBlocking {
        use("en", "US")
        assertEquals("Add habit", getString(Res.string.add_habit))
        assertEquals("You're all done for today!", getString(Res.string.no_habits_left_to_do))
    }

    @Test
    fun resolvesPositionalAndPluralStrings() = runBlocking {
        use("en", "US")
        assertEquals("2 times in 3 days", getString(Res.string.x_times_per_y_days, 2, 3))
        assertEquals("Habit deleted", getPluralString(Res.plurals.toast_habits_deleted, 1))
        assertEquals("Habits deleted", getPluralString(Res.plurals.toast_habits_deleted, 2))
    }

    @Test
    fun resolvesTranslationsAndLegacyLanguageCodes() = runBlocking {
        val english = run { use("en", "US"); getString(Res.string.add_habit) }
        for ((language, country) in listOf(
            "es" to "ES", "pt" to "BR", "zh" to "CN", "id" to "ID", "in" to "ID", "he" to "IL", "iw" to "IL",
            "nb" to "NO", "no" to "NO"
        )) {
            use(language, country)
            assertNotEquals(english, getString(Res.string.add_habit), "$language-$country")
        }
        use("es", "ES")
        assertEquals("Agregar hábito", getString(Res.string.add_habit))
    }
}
