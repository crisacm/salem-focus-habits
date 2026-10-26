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
import WidgetKit

/// Runs one change of the board on a habit and tells the widget to draw again. Throws when the database is not ready or
/// the habit is gone (the board returns nil for both, so `loadHabits` is asked first).
private func change(_ habit: HabitEntity, _ action: (HabitsBoard, Int64) throws -> BoardHabit?) throws -> BoardHabit {
    guard let board = HabitsBoardFactory.shared.create(defaultReminderBody: String(localized: "Have you completed this habit today?")) else { throw HabitShortcutError.openApp }
    defer { board.close() }
    // One query, where today() computes every habit
    guard let ready = try? board.isReady() else { throw HabitShortcutError.unavailable }
    if !ready { throw HabitShortcutError.openApp }
    guard let result = try action(board, Int64(habit.id)) else { throw HabitShortcutError.unknownHabit }
    WidgetCenter.shared.reloadAllTimelines()
    return result
}

private func status(_ habit: BoardHabit) -> IntentDialog {
    if habit.isNumerical {
        let amount = habit.number.formatted()
        let target = habit.target.formatted()
        return IntentDialog("\(habit.name): \(amount) / \(target)")
    }
    return habit.isDone ? IntentDialog("\(habit.name) is done") : IntentDialog("\(habit.name) is not done")
}

struct CheckHabitIntent: AppIntent {
    static let title: LocalizedStringResource = "Check habit"
    static let description = IntentDescription("Marks a habit as done for today.")

    @Parameter(title: "Habit") var habit: HabitEntity

    static var parameterSummary: some ParameterSummary { Summary("Check \(\.$habit)") }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        .result(dialog: status(try change(habit) { try $0.check(id: $1) }))
    }
}

struct UncheckHabitIntent: AppIntent {
    static let title: LocalizedStringResource = "Uncheck habit"
    static let description = IntentDescription("Marks a habit as not done for today.")

    @Parameter(title: "Habit") var habit: HabitEntity

    static var parameterSummary: some ParameterSummary { Summary("Uncheck \(\.$habit)") }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        .result(dialog: status(try change(habit) { try $0.uncheck(id: $1) }))
    }
}

struct ToggleHabitShortcutIntent: AppIntent {
    static let title: LocalizedStringResource = "Toggle habit"
    static let description = IntentDescription("Moves a habit to its next value for today, as a tap in the app does.")

    @Parameter(title: "Habit") var habit: HabitEntity

    static var parameterSummary: some ParameterSummary { Summary("Toggle \(\.$habit)") }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        .result(dialog: status(try change(habit) { try $0.toggle(id: $1) }))
    }
}

struct IncrementHabitShortcutIntent: AppIntent {
    static let title: LocalizedStringResource = "Add one to habit"
    static let description = IntentDescription("Adds one unit to a numerical habit for today.")

    @Parameter(title: "Habit") var habit: HabitEntity

    static var parameterSummary: some ParameterSummary { Summary("Add one to \(\.$habit)") }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        .result(dialog: status(try change(habit) { try $0.increment(id: $1) }))
    }
}

struct DecrementHabitIntent: AppIntent {
    static let title: LocalizedStringResource = "Subtract one from habit"
    static let description = IntentDescription("Takes one unit from a numerical habit for today, never below zero.")

    @Parameter(title: "Habit") var habit: HabitEntity

    static var parameterSummary: some ParameterSummary { Summary("Subtract one from \(\.$habit)") }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        .result(dialog: status(try change(habit) { try $0.decrement(id: $1) }))
    }
}
