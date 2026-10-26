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

/// Checks or unchecks a habit of today from the widget (the "Toggle" of the app: yes, no, and skip or unknown when the
/// settings allow them).
struct ToggleHabitIntent: AppIntent {
    static let title: LocalizedStringResource = "Toggle habit"
    // Only the widget uses it; the shortcuts have their own intents with a habit to choose
    static var isDiscoverable: Bool { false }

    @Parameter(title: "Habit") var habitId: Int

    init() {}

    init(habitId: Int) {
        self.habitId = habitId
    }

    func perform() async throws -> some IntentResult {
        _ = HabitsBoardFactory.shared.create()?.toggle(id: Int64(habitId))
        return .result()
    }
}

/// Adds one unit to a numerical habit of today from the widget.
struct IncrementHabitIntent: AppIntent {
    static let title: LocalizedStringResource = "Add one to habit"
    static var isDiscoverable: Bool { false }

    @Parameter(title: "Habit") var habitId: Int

    init() {}

    init(habitId: Int) {
        self.habitId = habitId
    }

    func perform() async throws -> some IntentResult {
        _ = HabitsBoardFactory.shared.create()?.increment(id: Int64(habitId))
        return .result()
    }
}
