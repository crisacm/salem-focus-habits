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

package io.github.crisacm.salem.utils

import android.text.format.DateFormat
import io.github.crisacm.salem.platform.time.DateFormats
import java.text.SimpleDateFormat
import java.util.Locale

fun String.toSimpleDataFormat(): SimpleDateFormat {
    val locale = Locale.getDefault()
    return DateFormats.fromSkeleton(DateFormat.getBestDateTimePattern(locale, this), locale)
}
