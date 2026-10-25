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

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeZoneForSecondsFromGMT

/**
 * [LocalDateFormatter] for iOS. Names come from the symbol tables of an NSDateFormatter in [locale]
 * (format context, like Calendar.getDisplayName on the JVM), so no date arithmetic or time zone is
 * involved. The tables are read once: create a new formatter when the locale changes.
 */
class AppleLocalDateFormatter(locale: NSLocale = NSLocale.currentLocale) : LocalDateFormatter {
    private val longWeekdays: List<String>
    private val shortWeekdays: List<String>
    private val narrowWeekdays: List<String>
    private val longMonths: List<String>
    private val shortMonths: List<String>
    private val mediumFormatter: NSDateFormatter

    init {
        val formatter = NSDateFormatter()
        formatter.calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian)
        formatter.locale = locale
        longWeekdays = formatter.weekdaySymbols.filterIsInstance<String>()
        shortWeekdays = formatter.shortWeekdaySymbols.filterIsInstance<String>()
        narrowWeekdays = formatter.veryShortWeekdaySymbols.filterIsInstance<String>()
        longMonths = formatter.monthSymbols.filterIsInstance<String>()
        shortMonths = formatter.shortMonthSymbols.filterIsInstance<String>()
        // A date has no time of day: it is written in GMT so the day does not move with the time zone of the device
        mediumFormatter = NSDateFormatter().apply {
            calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian)
            this.locale = locale
            dateStyle = NSDateFormatterMediumStyle
            timeStyle = NSDateFormatterNoStyle
            timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)
        }
    }

    override fun narrowWeekdayName(weekday: DayOfWeek) = narrowWeekdays[weekday.daysSinceSunday]

    override fun narrowWeekdayName(date: LocalDate) = narrowWeekdayName(date.dayOfWeek)

    override fun shortWeekdayName(weekday: DayOfWeek) = shortWeekdays[weekday.daysSinceSunday]

    override fun shortWeekdayName(date: LocalDate) = shortWeekdayName(date.dayOfWeek)

    /**
     * The long name when it has 3 characters or fewer (for example Japanese), as on the JVM.
     */
    override fun shortMonthName(date: LocalDate): String {
        val long = longMonthName(date)
        return if (long.length <= 3) long else shortMonths[date.month - 1]
    }

    override fun longWeekdayName(weekday: DayOfWeek) = longWeekdays[weekday.daysSinceSunday]

    override fun longMonthName(date: LocalDate) = longMonths[date.month - 1]

    override fun longFormat(date: LocalDate): String =
        mediumFormatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(date.daysSince2000 * SECONDS_PER_DAY + SECONDS_2000))

    private companion object {
        const val SECONDS_PER_DAY = 86_400.0

        // 2000-01-01T00:00:00Z in the Unix epoch
        const val SECONDS_2000 = 946_684_800.0
    }
}
