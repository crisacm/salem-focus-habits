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

/// The language of the app: "en", "es" or "" for the language of the device. It is observable, so the screens that
/// wrote a text with [L] or a date with the helpers below are drawn again when it changes.
@Observable
final class AppLanguage: @unchecked Sendable {
    static let shared = AppLanguage()

    private(set) var tag = ""
    /// The strings of the language, or nil when there is none for it (the texts then are the English ones of the code).
    private(set) var bundle: Bundle? = Bundle.main
    private(set) var locale = Locale.current

    func apply(_ newTag: String) {
        guard newTag != tag else { return }
        tag = newTag
        if newTag.isEmpty {
            bundle = Bundle.main
            locale = Locale.current
        } else {
            let folder = Bundle.main.localizations.first { $0 == newTag || $0.hasPrefix(newTag + "-") }
            bundle = folder
                .flatMap { Bundle.main.path(forResource: $0, ofType: "lproj") }
                .flatMap { Bundle(path: $0) }
            locale = Locale(identifier: newTag)
        }
    }
}

func L(_ key: String, _ fallback: String) -> String {
    guard let bundle = AppLanguage.shared.bundle else { return fallback }
    let value = bundle.localizedString(forKey: key, value: fallback, table: nil)
    return value == key ? fallback : value
}

/// A date formatter in the language of the app; set the template after this call, as it depends on the locale.
func appDateFormatter() -> DateFormatter {
    let formatter = DateFormatter()
    formatter.locale = AppLanguage.shared.locale
    return formatter
}

/// The calendar of the device with the names of the days in the language of the app.
func appCalendar() -> Calendar {
    var calendar = Calendar.current
    calendar.locale = AppLanguage.shared.locale
    return calendar
}

/// The time of day the way the language of the app writes it.
func appTimeText(_ date: Date) -> String {
    let formatter = appDateFormatter()
    formatter.dateStyle = .none
    formatter.timeStyle = .short
    return formatter.string(from: date)
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
