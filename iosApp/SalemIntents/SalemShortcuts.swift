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

/// The phrases for Siri and Spotlight. Every intent also shows up as an action in the Shortcuts app.
struct SalemShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: ToggleHabitShortcutIntent(),
            phrases: [
                "Toggle a habit in \(.applicationName)",
                "Check off a habit in \(.applicationName)"
            ],
            shortTitle: "Toggle habit",
            systemImageName: "checkmark.circle"
        )
        AppShortcut(
            intent: IncrementHabitShortcutIntent(),
            phrases: ["Add one to a habit in \(.applicationName)"],
            shortTitle: "Add one to habit",
            systemImageName: "plus.circle"
        )
    }
}
