//
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

import AppIntents
import SalemExtras

/// A habit that a shortcut can act on. The habits come from the database that the app shares through the App Group, so
/// this extension never needs the app to be running.
struct HabitEntity: AppEntity {
    static let typeDisplayRepresentation: TypeDisplayRepresentation = "Habit"
    static let defaultQuery = HabitQuery()

    let id: Int
    let name: String

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(title: "\(name)")
    }
}

struct HabitQuery: EntityStringQuery {
    func entities(for identifiers: [Int]) async throws -> [HabitEntity] {
        try loadHabits().filter { identifiers.contains($0.id) }
    }

    func entities(matching string: String) async throws -> [HabitEntity] {
        try loadHabits().filter { $0.name.localizedCaseInsensitiveContains(string) }
    }

    func suggestedEntities() async throws -> [HabitEntity] {
        try loadHabits()
    }
}

/// Why a shortcut could not change a habit.
enum HabitShortcutError: Error, CustomLocalizedStringResourceConvertible {
    case openApp
    case unknownHabit
    case unavailable

    var localizedStringResource: LocalizedStringResource {
        switch self {
        case .openApp: return "Open Salem first so it can set up your habits."
        case .unknownHabit: return "That habit no longer exists."
        case .unavailable: return "Salem could not use its data right now. Try again in a moment."
        }
    }
}

/// The habits of today in the order of the app. The board tells apart a database that is not ready (the app has to run
/// first) from a list that is just empty.
func loadHabits() throws -> [HabitEntity] {
    guard let board = HabitsBoardFactory.shared.create(defaultReminderBody: String(localized: "Have you completed this habit today?")) else { throw HabitShortcutError.openApp }
    defer { board.close() }
    // The board throws what SQLite reports (a locked or unreadable database); the extension must not end with it
    guard let state = try? board.today() else { throw HabitShortcutError.unavailable }
    if state.needsApp { throw HabitShortcutError.openApp }
    return state.habits.map { HabitEntity(id: Int($0.id), name: $0.name) }
}
