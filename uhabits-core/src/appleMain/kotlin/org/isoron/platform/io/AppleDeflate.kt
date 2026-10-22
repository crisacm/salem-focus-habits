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

@file:OptIn(ExperimentalForeignApi::class)

package org.isoron.platform.io

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import platform.zlib.ZLIB_VERSION
import platform.zlib.Z_DEFLATED
import platform.zlib.Z_FINISH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.crc32
import platform.zlib.deflate
import platform.zlib.deflateBound
import platform.zlib.deflateEnd
import platform.zlib.deflateInit2_
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.z_stream

// Raw DEFLATE (RFC 1951, no zlib header): negative window bits.
private const val RAW_WINDOW_BITS = -15
private const val COMPRESSION_LEVEL = 6
private const val DEFAULT_MEM_LEVEL = 8
private const val DEFAULT_STRATEGY = 0

// The empty input compressed by zlib: a final fixed-Huffman block with no symbols.
private val EMPTY_DEFLATE = byteArrayOf(0x03, 0x00)

/**
 * CRC-32 (IEEE) of [bytes], as stored in zip headers.
 */
internal fun crc32Of(bytes: ByteArray): Int {
    if (bytes.isEmpty()) return 0
    return bytes.usePinned { crc32(0u, it.addressOf(0).reinterpret(), bytes.size.convert()).toInt() }
}

/**
 * Compresses [input] as a raw DEFLATE stream.
 */
internal fun deflateRaw(input: ByteArray): ByteArray {
    if (input.isEmpty()) return EMPTY_DEFLATE
    return memScoped {
        val stream = alloc<z_stream>()
        val init = deflateInit2_(
            stream.ptr,
            COMPRESSION_LEVEL,
            Z_DEFLATED,
            RAW_WINDOW_BITS,
            DEFAULT_MEM_LEVEL,
            DEFAULT_STRATEGY,
            ZLIB_VERSION,
            sizeOf<z_stream>().toInt()
        )
        check(init == Z_OK) { "deflateInit2 failed: $init" }
        try {
            val out = ByteArray(deflateBound(stream.ptr, input.size.convert()).toInt())
            input.usePinned { source ->
                out.usePinned { target ->
                    stream.next_in = source.addressOf(0).reinterpret()
                    stream.avail_in = input.size.convert()
                    stream.next_out = target.addressOf(0).reinterpret()
                    stream.avail_out = out.size.convert()
                    val result = deflate(stream.ptr, Z_FINISH)
                    check(result == Z_STREAM_END) { "deflate failed: $result" }
                }
            }
            out.copyOf(stream.total_out.toInt())
        } finally {
            deflateEnd(stream.ptr)
        }
    }
}

/**
 * Decompresses a raw DEFLATE stream that must produce exactly [expectedSize] bytes.
 * Throws [IllegalArgumentException] if the data is corrupt, truncated or has another size.
 */
internal fun inflateRaw(input: ByteArray, expectedSize: Int): ByteArray {
    require(input.isNotEmpty()) { "Empty DEFLATE data" }
    return memScoped {
        val stream = alloc<z_stream>()
        val init = inflateInit2_(stream.ptr, RAW_WINDOW_BITS, ZLIB_VERSION, sizeOf<z_stream>().toInt())
        check(init == Z_OK) { "inflateInit2 failed: $init" }
        try {
            // One spare byte tells data that is larger than expected from data that fits exactly
            val out = ByteArray(expectedSize + 1)
            val result = input.usePinned { source ->
                out.usePinned { target ->
                    stream.next_in = source.addressOf(0).reinterpret()
                    stream.avail_in = input.size.convert()
                    stream.next_out = target.addressOf(0).reinterpret()
                    stream.avail_out = out.size.convert()
                    inflate(stream.ptr, Z_FINISH)
                }
            }
            require(result == Z_STREAM_END) { "Corrupt or truncated DEFLATE data: $result" }
            require(stream.total_out.toInt() == expectedSize) {
                "Expected $expectedSize bytes but the data holds ${stream.total_out}"
            }
            out.copyOf(expectedSize)
        } finally {
            inflateEnd(stream.ptr)
        }
    }
}

/**
 * A moment in the packed 16 bit fields of a zip header.
 */
internal data class DosDateTime(val time: Int, val date: Int)

/**
 * Converts a local date and time to the MS-DOS fields used by zip: two second resolution and
 * years counted from 1980 (earlier years are clamped to 1980).
 */
internal fun dosDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): DosDateTime {
    val time = (hour shl 11) or (minute shl 5) or (second / 2)
    val date = ((maxOf(year, 1980) - 1980) shl 9) or (month shl 5) or day
    return DosDateTime(time, date)
}
