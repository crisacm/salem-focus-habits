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
import SwiftUI
import WidgetKit

/// A habit as the widget draws it.
struct HabitLine: Identifiable {
    let id: Int64
    let name: String
    let color: Color
    let isDone: Bool
    let isNumerical: Bool
    /// "2/3 km" for a numerical habit.
    let amount: String
    let score: Int

    init(id: Int64, name: String, color: Color, isDone: Bool, isNumerical: Bool, amount: String, score: Int) {
        self.id = id
        self.name = name
        self.color = color
        self.isDone = isDone
        self.isNumerical = isNumerical
        self.amount = amount
        self.score = score
    }

    init(_ habit: BoardHabit) {
        let argb = UInt32(truncatingIfNeeded: habit.colorArgb)
        self.init(
            id: habit.id,
            name: habit.name,
            color: Color(
                red: Double((argb >> 16) & 0xFF) / 255,
                green: Double((argb >> 8) & 0xFF) / 255,
                blue: Double(argb & 0xFF) / 255
            ),
            isDone: habit.isDone,
            isNumerical: habit.isNumerical,
            amount: habit.isNumerical
                ? "\(HabitLine.format(habit.number))/\(HabitLine.format(habit.target)) \(habit.unit)"
                : "",
            score: Int(habit.scorePercent)
        )
    }

    private static func format(_ value: Double) -> String {
        let formatter = NumberFormatter()
        formatter.maximumFractionDigits = 1
        return formatter.string(from: NSNumber(value: value)) ?? "\(value)"
    }
}

struct TodayEntry: TimelineEntry {
    let date: Date
    let lines: [HabitLine]
    /// The app has not created its database yet, or is older than this widget.
    let needsApp: Bool
}

struct TodayProvider: TimelineProvider {
    func placeholder(in context: Context) -> TodayEntry {
        TodayEntry(
            date: Date(),
            lines: [
                HabitLine(id: 1, name: "Meditate", color: .orange, isDone: true, isNumerical: false, amount: "", score: 82),
                HabitLine(id: 2, name: "Read", color: .blue, isDone: false, isNumerical: false, amount: "", score: 64),
                HabitLine(id: 3, name: "Run", color: .green, isDone: false, isNumerical: true, amount: "1/3 km", score: 40)
            ],
            needsApp: false
        )
    }

    func getSnapshot(in context: Context, completion: @escaping (TodayEntry) -> Void) {
        completion(context.isPreview ? placeholder(in: context) : load())
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<TodayEntry>) -> Void) {
        // Draw again in an hour, or just after midnight if that comes first, when the day of the habits changes
        let now = Date()
        let calendar = Calendar.current
        let tomorrow = calendar.startOfDay(for: now.addingTimeInterval(24 * 60 * 60))
        let afterMidnight = tomorrow.addingTimeInterval(60)
        completion(Timeline(entries: [load()], policy: .after(min(now.addingTimeInterval(60 * 60), afterMidnight))))
    }

    private func load() -> TodayEntry {
        guard let board = HabitsBoardFactory.shared.create(defaultReminderBody: String(localized: "Have you completed this habit today?")) else {
            return TodayEntry(date: Date(), lines: [], needsApp: true)
        }
        defer { board.close() }
        // A failure (the database is locked, a read error) looks like a database that is not ready: open the app
        guard let state = try? board.today() else {
            return TodayEntry(date: Date(), lines: [], needsApp: true)
        }
        return TodayEntry(date: Date(), lines: state.habits.map(HabitLine.init), needsApp: state.needsApp)
    }
}

struct HabitLineView: View {
    let line: HabitLine

    var body: some View {
        HStack(spacing: 8) {
            if line.isNumerical {
                Button(intent: IncrementHabitIntent(habitId: Int(line.id))) {
                    Image(systemName: line.isDone ? "plus.circle.fill" : "plus.circle")
                        .foregroundStyle(line.color)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text("Add one to \(line.name)"))
            } else {
                Button(intent: ToggleHabitIntent(habitId: Int(line.id))) {
                    Image(systemName: line.isDone ? "checkmark.circle.fill" : "circle")
                        .foregroundStyle(line.color)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(line.isDone ? "\(line.name), done" : "\(line.name), not done"))
            }
            VStack(alignment: .leading, spacing: 0) {
                Text(line.name)
                    .font(.subheadline)
                    .lineLimit(1)
                if line.isNumerical {
                    Text(line.amount)
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }
            Spacer(minLength: 4)
            Text("\(line.score)%")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
    }
}

struct TodayView: View {
    let entry: TodayEntry
    @Environment(\.widgetFamily) private var family

    private var limit: Int {
        switch family {
        case .systemSmall: return 3
        case .systemMedium: return 4
        default: return 8
        }
    }

    var body: some View {
        if entry.needsApp {
            message("Open Salem to get started.")
        } else if entry.lines.isEmpty {
            message("No habits for today.")
        } else {
            VStack(alignment: .leading, spacing: 8) {
                ForEach(entry.lines.prefix(limit)) { line in
                    HabitLineView(line: line)
                }
                Spacer(minLength: 0)
            }
        }
    }

    private func message(_ text: LocalizedStringKey) -> some View {
        Text(text)
            .font(.footnote)
            .multilineTextAlignment(.center)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// The habits of today, with a button to check each one.
struct TodayWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "TodayWidget", provider: TodayProvider()) { entry in
            TodayView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
        }
        .configurationDisplayName("Today")
        .description("Check off your habits for today.")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}
