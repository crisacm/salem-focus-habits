// Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
//
// This file is part of Loop Habit Tracker.
//
// Loop Habit Tracker is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Loop Habit Tracker is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
//
// You should have received a copy of the GNU General Public License along
// with this program. If not, see <http://www.gnu.org/licenses/>.
//

import SwiftUI

func L(_ key: String, _ fallback: String) -> String {
    let value = Bundle.main.localizedString(forKey: key, value: fallback, table: nil)
    return value == key ? fallback : value
}

func habitColor(_ argb: Int64) -> Color {
    let value = UInt32(truncatingIfNeeded: argb)
    let alpha = Double((value >> 24) & 0xff) / 255
    let red = Double((value >> 16) & 0xff) / 255
    let green = Double((value >> 8) & 0xff) / 255
    let blue = Double(value & 0xff) / 255
    return Color(.sRGB, red: red, green: green, blue: blue, opacity: alpha == 0 ? 1 : alpha)
}

func date(fromDaysSince2000 day: Int) -> Date {
    var parts = DateComponents()
    parts.year = 2000
    parts.month = 1
    parts.day = 1
    let base = Calendar(identifier: .gregorian).date(from: parts) ?? Date()
    return Calendar(identifier: .gregorian).date(byAdding: .day, value: day, to: base) ?? base
}

enum EntryValue {
    static let unknown = -1
    static let no = 0
    static let yesAuto = 1
    static let yesManual = 2
    static let skip = 3
}
