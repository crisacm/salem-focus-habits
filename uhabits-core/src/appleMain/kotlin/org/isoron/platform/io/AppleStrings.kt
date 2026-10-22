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

package org.isoron.platform.io

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleDecimalSeparator
import platform.Foundation.currentLocale

actual fun format(format: String, arg: String): String = formatPrintf(format, arg)

actual fun format(format: String, arg: Int): String = formatPrintf(format, arg)

actual fun format(format: String, arg: Double): String = formatPrintf(format, arg, localeDecimalSeparator())

actual fun formatInvariant(format: String, arg: Int): String = formatPrintf(format, arg)

actual fun formatInvariant(format: String, arg: Double): String = formatPrintf(format, arg)

private fun localeDecimalSeparator(): Char {
    val separator = NSLocale.currentLocale.objectForKey(NSLocaleDecimalSeparator) as? String
    return separator?.firstOrNull() ?: '.'
}
