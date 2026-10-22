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

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

private const val LOCAL_HEADER_SIGNATURE = 0x04034b50
private const val CENTRAL_HEADER_SIGNATURE = 0x02014b50
private const val END_SIGNATURE = 0x06054b50
private const val VERSION = 20
private const val FLAG_UTF8_NAMES = 0x0800
private const val METHOD_DEFLATED = 8
private const val MAX_ENTRIES = 0xFFFF

actual class ZipReader actual constructor(bytes: ByteArray) {
    actual suspend fun entries(): List<ZipEntry> = TODO("iOS: step 4")
}

/**
 * Writes a zip archive in memory: every entry is DEFLATED and its UTF-8 name is flagged as such, the
 * sizes are known up front so there are no data descriptors. ZIP64 is not supported, so the archive
 * must stay under 65 535 entries and 2 GiB. Entry names must be unique, as in java.util.zip.
 */
actual class ZipWriter private constructor(private val fixedStamp: DosDateTime?) {
    private class Item(val name: ByteArray, val crc: Int, val size: Int, val deflated: ByteArray)

    private val items = ArrayList<Item>()
    private val names = HashSet<String>()
    private var result: ByteArray? = null

    actual constructor() : this(null)

    /**
     * Writer with a fixed modification time, for reproducible archives in tests.
     */
    internal constructor(dosTime: Int, dosDate: Int) : this(DosDateTime(dosTime, dosDate))

    actual fun addEntry(name: String, content: String) {
        check(result == null) { "The archive was already written" }
        val nameBytes = name.encodeToByteArray()
        require(nameBytes.isNotEmpty() && nameBytes.size <= 0xFFFF) { "Invalid entry name length: ${nameBytes.size}" }
        require(names.add(name)) { "Duplicate entry: $name" }
        check(items.size < MAX_ENTRIES) { "Too many entries for a zip without ZIP64" }
        val data = content.encodeToByteArray()
        items.add(Item(nameBytes, crc32Of(data), data.size, deflateRaw(data)))
    }

    actual suspend fun toBytes(): ByteArray {
        result?.let { return it }
        val stamp = fixedStamp ?: currentDosDateTime()
        val out = ByteSink()
        val offsets = IntArray(items.size)
        items.forEachIndexed { index, item ->
            offsets[index] = out.size
            out.int(LOCAL_HEADER_SIGNATURE)
            out.short(VERSION)
            out.short(FLAG_UTF8_NAMES)
            out.short(METHOD_DEFLATED)
            out.short(stamp.time)
            out.short(stamp.date)
            out.int(item.crc)
            out.int(item.deflated.size)
            out.int(item.size)
            out.short(item.name.size)
            out.short(0)
            out.bytes(item.name)
            out.bytes(item.deflated)
        }
        val directoryOffset = out.size
        items.forEachIndexed { index, item ->
            out.int(CENTRAL_HEADER_SIGNATURE)
            out.short(VERSION)
            out.short(VERSION)
            out.short(FLAG_UTF8_NAMES)
            out.short(METHOD_DEFLATED)
            out.short(stamp.time)
            out.short(stamp.date)
            out.int(item.crc)
            out.int(item.deflated.size)
            out.int(item.size)
            out.short(item.name.size)
            out.short(0)
            out.short(0)
            out.short(0)
            out.short(0)
            out.int(0)
            out.int(offsets[index])
            out.bytes(item.name)
        }
        val directorySize = out.size - directoryOffset
        out.int(END_SIGNATURE)
        out.short(0)
        out.short(0)
        out.short(items.size)
        out.short(items.size)
        out.int(directorySize)
        out.int(directoryOffset)
        out.short(0)
        return out.toByteArray().also { result = it }
    }
}

/**
 * Growable little-endian byte buffer. Fails before the size reaches 2 GiB.
 */
private class ByteSink {
    private var data = ByteArray(1024)
    var size = 0
        private set

    fun short(value: Int) {
        ensure(2)
        data[size++] = value.toByte()
        data[size++] = (value shr 8).toByte()
    }

    fun int(value: Int) {
        short(value)
        short(value shr 16)
    }

    fun bytes(value: ByteArray) {
        ensure(value.size)
        value.copyInto(data, size)
        size += value.size
    }

    fun toByteArray(): ByteArray = data.copyOf(size)

    private fun ensure(extra: Int) {
        val needed = size.toLong() + extra
        check(needed < Int.MAX_VALUE) { "The archive is too large for a zip without ZIP64" }
        if (needed <= data.size) return
        data = data.copyOf(maxOf(needed, minOf(data.size * 2L, Int.MAX_VALUE.toLong() - 1)).toInt())
    }
}

internal fun currentDosDateTime(): DosDateTime {
    val units = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or
        NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond
    val parts = NSCalendar.currentCalendar.components(units, fromDate = NSDate())
    return dosDateTime(
        parts.year.toInt(),
        parts.month.toInt(),
        parts.day.toInt(),
        parts.hour.toInt(),
        parts.minute.toInt(),
        parts.second.toInt()
    )
}
