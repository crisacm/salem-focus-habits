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
private const val METHOD_STORED = 0
private const val METHOD_DEFLATED = 8
private const val FLAG_ENCRYPTED = 1
private const val LOCAL_HEADER_SIZE = 30
private const val CENTRAL_HEADER_SIZE = 46
private const val END_SIZE = 22
private const val MAX_ENTRY_SIZE = 256L * 1024 * 1024
private const val MAX_ENTRIES = 0xFFFF

/**
 * Reads a whole zip archive from memory. It follows the central directory, so archives written with
 * data descriptors (like the JDK does) work, and it supports STORED and DEFLATED entries with a
 * CRC-32 check. Directory entries are returned with empty content, as java.util.zip does.
 *
 * Unlike the JVM reader, which returns an empty list for bytes that are not a zip, anything that is
 * not a valid archive (not a zip, truncated, corrupt, ZIP64, encrypted, an unknown method, or an entry
 * over 256 MiB) throws [IllegalArgumentException].
 */
actual class ZipReader actual constructor(private val bytes: ByteArray) {
    actual suspend fun entries(): List<ZipEntry> {
        val endAt = findEndOfCentralDirectory()
        val count = bytes.u16(endAt + 10)
        val directorySize = bytes.u32(endAt + 12)
        val directoryOffset = bytes.u32(endAt + 16)
        require(count != 0xFFFF && directoryOffset != 0xFFFFFFFFL) { "ZIP64 archives are not supported" }
        require(directoryOffset + directorySize <= endAt) { "Invalid central directory" }
        val entries = ArrayList<ZipEntry>(count)
        var at = directoryOffset.toInt()
        repeat(count) {
            require(at + CENTRAL_HEADER_SIZE <= endAt && bytes.u32(at) == CENTRAL_HEADER_SIGNATURE.toLong()) {
                "Invalid central directory entry"
            }
            val nameLength = bytes.u16(at + 28)
            val skipped = bytes.u16(at + 30) + bytes.u16(at + 32)
            require(at + CENTRAL_HEADER_SIZE + nameLength + skipped <= endAt) { "Invalid central directory entry" }
            val name = bytes.decodeToString(at + CENTRAL_HEADER_SIZE, at + CENTRAL_HEADER_SIZE + nameLength)
            entries.add(ZipEntry(name, readContent(at, directoryOffset.toInt())))
            at += CENTRAL_HEADER_SIZE + nameLength + skipped
        }
        return entries
    }

    private fun findEndOfCentralDirectory(): Int {
        require(bytes.size >= END_SIZE) { "Not a zip archive" }
        val lowest = maxOf(0, bytes.size - END_SIZE - 0xFFFF)
        for (at in bytes.size - END_SIZE downTo lowest) {
            if (bytes.u32(at) == END_SIGNATURE.toLong()) return at
        }
        throw IllegalArgumentException("Not a zip archive")
    }

    private fun readContent(headerAt: Int, directoryOffset: Int): String {
        val flags = bytes.u16(headerAt + 8)
        val method = bytes.u16(headerAt + 10)
        val crc = bytes.u32(headerAt + 16)
        val compressedSize = bytes.u32(headerAt + 20)
        val size = bytes.u32(headerAt + 24)
        val localAt = bytes.u32(headerAt + 42)
        require(flags and FLAG_ENCRYPTED == 0) { "Encrypted entries are not supported" }
        require(compressedSize != 0xFFFFFFFFL && size != 0xFFFFFFFFL && localAt != 0xFFFFFFFFL) {
            "ZIP64 archives are not supported"
        }
        require(size <= MAX_ENTRY_SIZE) { "Entry too large: $size bytes" }
        require(localAt + LOCAL_HEADER_SIZE <= directoryOffset && bytes.u32(localAt.toInt()) == LOCAL_HEADER_SIGNATURE.toLong()) {
            "Invalid local header"
        }
        val dataAt = localAt + LOCAL_HEADER_SIZE + bytes.u16(localAt.toInt() + 26) + bytes.u16(localAt.toInt() + 28)
        require(dataAt + compressedSize <= directoryOffset) { "Entry data is out of range" }
        val stored = bytes.copyOfRange(dataAt.toInt(), (dataAt + compressedSize).toInt())
        val data = when (method) {
            METHOD_STORED -> {
                require(compressedSize == size) { "Stored entry with different sizes" }
                stored
            }
            METHOD_DEFLATED -> inflateRaw(stored, size.toInt())
            else -> throw IllegalArgumentException("Unsupported compression method: $method")
        }
        require(crc32Of(data).toLong() and 0xFFFFFFFFL == crc) { "CRC-32 mismatch" }
        return data.decodeToString()
    }
}

private fun ByteArray.u16(at: Int): Int {
    require(at >= 0 && at + 2 <= size) { "Truncated zip archive" }
    return (this[at].toInt() and 0xFF) or ((this[at + 1].toInt() and 0xFF) shl 8)
}

private fun ByteArray.u32(at: Int): Long = u16(at).toLong() or (u16(at + 2).toLong() shl 16)

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
