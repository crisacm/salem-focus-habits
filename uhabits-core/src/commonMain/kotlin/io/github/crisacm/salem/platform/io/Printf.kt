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

package io.github.crisacm.salem.platform.io

/**
 * Minimal printf-style formatter for platforms without java.util.Formatter. It supports a single
 * argument and the conversions `%d`, `%f` and `%s` with the flags `-` and `0`, a width and (for
 * `%f` and `%s`) a precision, plus the literal `%%`. Anything else throws
 * [IllegalArgumentException].
 *
 * It follows java.util.Formatter: `%f` rounds HALF_UP on the shortest decimal representation of
 * the value, so `2.5` with `%.0f` is `3`, as on Android. Digits are always ASCII; the decimal
 * separator is [decimalSeparator].
 */
internal fun formatPrintf(format: String, arg: Any, decimalSeparator: Char = '.'): String {
    val out = StringBuilder()
    var argUsed = false
    var i = 0
    while (i < format.length) {
        val c = format[i++]
        if (c != '%') {
            out.append(c)
            continue
        }
        var leftAlign = false
        var zeroPad = false
        while (i < format.length && (format[i] == '-' || format[i] == '0')) {
            if (format[i] == '-') leftAlign = true else zeroPad = true
            i++
        }
        var width = -1
        while (i < format.length && format[i].isDigit()) {
            width = maxOf(width, 0) * 10 + (format[i++] - '0')
        }
        var precision = -1
        if (i < format.length && format[i] == '.') {
            i++
            precision = 0
            while (i < format.length && format[i].isDigit()) {
                precision = precision * 10 + (format[i++] - '0')
            }
        }
        require(i < format.length) { "Incomplete format specifier in '$format'" }
        val conversion = format[i++]
        if (conversion == '%') {
            out.append('%')
            continue
        }
        require(!argUsed) { "Only one argument is supported: '$format'" }
        argUsed = true
        require(!(leftAlign && zeroPad)) { "Flags '-' and '0' conflict in '$format'" }
        require(!(zeroPad && width < 0)) { "Flag '0' needs a width in '$format'" }
        when (conversion) {
            'd' -> {
                val value = when (arg) {
                    is Int -> arg.toLong()
                    is Long -> arg
                    else -> throw IllegalArgumentException("%d needs an integer: '$format'")
                }
                require(precision < 0) { "%d has no precision: '$format'" }
                val digits = if (value < 0) value.toString().substring(1) else value.toString()
                out.append(pad(if (value < 0) "-" else "", digits, width, leftAlign, zeroPad))
            }
            'f' -> {
                require(arg is Double) { "%f needs a double: '$format'" }
                val negative = arg < 0 || (arg == 0.0 && 1.0 / arg < 0)
                val sign = if (negative) "-" else ""
                when {
                    arg.isNaN() -> out.append(pad("", "NaN", width, leftAlign, false))
                    arg.isInfinite() -> out.append(pad(sign, "Infinity", width, leftAlign, false))
                    else -> {
                        val digits = fixedPoint(if (negative) -arg else arg, if (precision < 0) 6 else precision)
                        out.append(pad(sign, digits.replace('.', decimalSeparator), width, leftAlign, zeroPad))
                    }
                }
            }
            's' -> {
                require(!zeroPad) { "Flag '0' is not valid for %s: '$format'" }
                val text = arg.toString()
                out.append(pad("", if (precision >= 0) text.take(precision) else text, width, leftAlign, false))
            }
            else -> throw IllegalArgumentException("Unsupported conversion '%$conversion' in '$format'")
        }
    }
    return out.toString()
}

private fun pad(sign: String, body: String, width: Int, leftAlign: Boolean, zeroPad: Boolean): String {
    val missing = width - sign.length - body.length
    return when {
        missing <= 0 -> sign + body
        leftAlign -> sign + body + " ".repeat(missing)
        zeroPad -> sign + "0".repeat(missing) + body
        else -> " ".repeat(missing) + sign + body
    }
}

/**
 * Formats a finite, non-negative [value] with [precision] decimals, rounding HALF_UP on the
 * shortest decimal representation (the digits of [Double.toString]).
 */
private fun fixedPoint(value: Double, precision: Int): String {
    val text = value.toString()
    val exponentAt = text.indexOf('E')
    val mantissa = if (exponentAt < 0) text else text.substring(0, exponentAt)
    val exponent = if (exponentAt < 0) 0 else text.substring(exponentAt + 1).toInt()
    val dotAt = mantissa.indexOf('.')
    var digits = mantissa.replace(".", "")
    var pointAt = (if (dotAt < 0) mantissa.length else dotAt) + exponent
    if (pointAt < 0) {
        digits = "0".repeat(-pointAt) + digits
        pointAt = 0
    }
    if (pointAt > digits.length) digits += "0".repeat(pointAt - digits.length)
    val integer = digits.substring(0, pointAt).ifEmpty { "0" }
    val fraction = digits.substring(pointAt)
    var all = integer + fraction.take(precision).padEnd(precision, '0')
    if (fraction.length > precision && fraction[precision] >= '5') all = increment(all)
    val integerLength = all.length - precision
    val head = all.substring(0, integerLength).trimStart('0').ifEmpty { "0" }
    return if (precision == 0) head else head + "." + all.substring(integerLength)
}

private fun increment(digits: String): String {
    val chars = digits.toCharArray()
    var i = chars.size - 1
    while (i >= 0) {
        if (chars[i] == '9') {
            chars[i] = '0'
            i--
        } else {
            chars[i] = chars[i] + 1
            return chars.concatToString()
        }
    }
    return "1" + chars.concatToString()
}
