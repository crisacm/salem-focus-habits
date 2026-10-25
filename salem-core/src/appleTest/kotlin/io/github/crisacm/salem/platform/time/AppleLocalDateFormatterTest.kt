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

package io.github.crisacm.salem.platform.time

import platform.Foundation.NSLocale
import kotlin.test.Test
import kotlin.test.assertEquals

class AppleLocalDateFormatterTest {
    private val us = AppleLocalDateFormatter(NSLocale("en_US"))

    @Test
    fun testEnglishWeekdayNames() {
        val days = DayOfWeek.entries
        assertEquals(
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            days.map { us.shortWeekdayName(it) }
        )
        assertEquals(
            listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"),
            days.map { us.longWeekdayName(it) }
        )
        assertEquals(
            listOf("S", "M", "T", "W", "T", "F", "S"),
            days.map { us.narrowWeekdayName(it) }
        )
    }

    @Test
    fun testWeekdayNamesFromDates() {
        // 2015-01-25 is a Sunday
        assertEquals("Sun", us.shortWeekdayName(LocalDate(2015, 1, 25)))
        assertEquals("Mon", us.shortWeekdayName(LocalDate(2015, 1, 26)))
        assertEquals("S", us.narrowWeekdayName(LocalDate(2015, 1, 25)))
        assertEquals("M", us.narrowWeekdayName(LocalDate(2015, 1, 26)))
    }

    @Test
    fun testEnglishMonthNames() {
        assertEquals(
            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
            (1..12).map { us.shortMonthName(LocalDate(2015, it, 1)) }
        )
        assertEquals(
            listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            ),
            (1..12).map { us.longMonthName(LocalDate(2015, it, 1)) }
        )
    }

    @Test
    fun testHonorsTheLocale() {
        val es = AppleLocalDateFormatter(NSLocale("es_CO"))
        assertEquals("domingo", es.longWeekdayName(DayOfWeek.SUNDAY))
        assertEquals("enero", es.longMonthName(LocalDate(2015, 1, 1)))
    }

    @Test
    fun testShortMonthUsesTheLongNameWhenItIsVeryShort() {
        // The long name of January in Japanese is "1月"; the short one must not be shorter than it.
        val ja = AppleLocalDateFormatter(NSLocale("ja_JP"))
        assertEquals(ja.longMonthName(LocalDate(2015, 1, 1)), ja.shortMonthName(LocalDate(2015, 1, 1)))
    }

    @Test
    fun testDefaultLocaleDoesNotCrash() {
        val formatter = AppleLocalDateFormatter()
        assertEquals(false, formatter.shortWeekdayName(DayOfWeek.MONDAY).isEmpty())
    }

    @Test
    fun testLongFormat() {
        assertEquals("Dec 1, 2020", us.longFormat(LocalDate(2020, 12, 1)))
        assertEquals("Jan 15, 2015", us.longFormat(LocalDate(2015, 1, 15)))
        assertEquals("Feb 28, 2000", us.longFormat(LocalDate(2000, 2, 28)))
    }
}
