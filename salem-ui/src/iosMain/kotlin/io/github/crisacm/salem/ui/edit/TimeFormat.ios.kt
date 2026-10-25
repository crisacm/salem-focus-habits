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

package io.github.crisacm.salem.ui.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeZoneForSecondsFromGMT

@Composable
actual fun rememberTimeFormat(): TimeFormat = remember {
    val locale = NSLocale.currentLocale
    val formatter = NSDateFormatter().apply {
        this.locale = locale
        dateStyle = NSDateFormatterNoStyle
        timeStyle = NSDateFormatterShortStyle
        timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)
    }
    // The template "j" asks for the preferred hour cycle of the locale.
    val pattern = NSDateFormatter.dateFormatFromTemplate("j", 0u, locale).orEmpty()
    TimeFormat(uses24Hour(pattern)) { hour, minute ->
        formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(((hour * 60 + minute) * 60).toDouble()))
    }
}
