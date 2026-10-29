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

import SalemKit
import SwiftUI

/// A card of the habit screen: a rounded panel with room around its content.
struct DetailCard<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        content
            .padding(20)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 24, style: .continuous))
    }
}

/// The title of a card with its subtitle, and a figure with a caption at the end.
struct CardTitle: View {
    let title: String
    var subtitle: String?
    var value: String?
    var unit: String?
    var caption: String?

    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            VStack(alignment: .leading, spacing: 3) {
                Text(title).font(.system(size: 18, weight: .semibold))
                if let subtitle {
                    Text(subtitle).font(.system(size: 12)).foregroundStyle(.secondary)
                }
            }
            Spacer(minLength: 0)
            if let value {
                VStack(alignment: .trailing, spacing: 2) {
                    Text(value)
                        .font(.system(size: 28, weight: .bold))
                        .lineLimit(1)
                        .minimumScaleFactor(0.7)
                    if let unit, !unit.isEmpty {
                        Text(unit)
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(.secondary)
                            .lineLimit(1)
                            .minimumScaleFactor(0.7)
                    }
                    if let caption {
                        Text(caption).font(.system(size: 12)).foregroundStyle(.secondary)
                    }
                }
                .layoutPriority(1)
            }
        }
    }
}

/// The icon of the habit inside the ring of its score, its name, its question and when it repeats and reminds.
struct DetailHeader: View {
    let detail: DetailSnapshot
    let tone: HabitTone

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 12) {
                Image(systemName: habitSymbol(id: detail.id))
                    .font(.system(size: 34, weight: .semibold))
                    .foregroundStyle(tone.accent)
                    .frame(width: 40, height: 40)
                    .accessibilityHidden(true)
                Text(detail.name)
                    .font(.system(size: 34, weight: .bold))
                    .lineLimit(2)
                    .minimumScaleFactor(0.7)
            }
            if !detail.question.isEmpty {
                Text(detail.question).font(.system(size: 15))
            }
            HStack(spacing: 14) {
                Label(frequencyText, systemImage: "calendar")
                if let reminderText {
                    Label(reminderText, systemImage: "bell")
                }
            }
            .font(.system(size: 13))
            .foregroundStyle(.secondary)
            .labelStyle(.titleAndIcon)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .combine)
    }

    private var frequencyText: String {
        let parts = detail.frequency.split(separator: "/").compactMap { Int($0) }
        guard parts.count == 2 else { return detail.frequency }
        return frequencySummaryText(numerator: parts[0], denominator: parts[1])
    }

    /// The reminder arrives as "07:30"; it is written the way the device writes a time.
    private var reminderText: String? {
        guard !detail.reminder.isEmpty else { return nil }
        let parts = detail.reminder.split(separator: ":").compactMap { Int($0) }
        guard parts.count == 2 else { return detail.reminder }
        var components = DateComponents()
        components.hour = parts[0]
        components.minute = parts[1]
        guard let date = Calendar.current.date(from: components) else { return detail.reminder }
        return appTimeText(date)
    }
}

/// The score today in large type with its ring, and the change in the last month and year and the total.
struct OverviewCard: View {
    let detail: DetailSnapshot
    let tone: HabitTone

    var body: some View {
        DetailCard {
            VStack(spacing: 18) {
                HStack {
                    VStack(alignment: .leading, spacing: 0) {
                        Text(L("score", "Score")).font(.system(size: 15)).foregroundStyle(.secondary)
                        Text("\(detail.scorePercent)%")
                            .font(.system(size: 48, weight: .bold))
                    }
                    Spacer()
                    Ring(fraction: Double(detail.scorePercent) / 100, tone: tone)
                        .frame(width: 74, height: 74)
                }
                HStack(spacing: 0) {
                    stat(detail.scoreMonth, L("month", "Month"))
                    Rectangle().fill(Color(.separator)).frame(width: 0.5, height: 40)
                    stat(detail.scoreYear, L("year", "Year"))
                    Rectangle().fill(Color(.separator)).frame(width: 0.5, height: 40)
                    stat("\(detail.total)", L("total", "Total"))
                }
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("\(L("score", "Score")) \(detail.scorePercent)%, \(L("month", "Month")) \(detail.scoreMonth), \(L("year", "Year")) \(detail.scoreYear), \(L("total", "Total")) \(detail.total)")
    }

    private func stat(_ value: String, _ title: String) -> some View {
        VStack(spacing: 2) {
            Text(value).font(.system(size: 24, weight: .semibold))
            Text(title).font(.system(size: 13)).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
}

/// The ring of the score of the overview: a track and an arc from the top.
private struct Ring: View {
    let fraction: Double
    let tone: HabitTone

    var body: some View {
        ZStack {
            Circle().strokeBorder(tone.track, lineWidth: 11)
            Circle()
                .inset(by: 5.5)
                .trim(from: 0, to: CGFloat(min(max(fraction, 0), 1)))
                .stroke(tone.accent, style: StrokeStyle(lineWidth: 11, lineCap: .round))
                .rotationEffect(.degrees(-90))
        }
    }
}

/// What is left to reach the target of the day, the week, the month, the quarter and the year; only for measurable habits.
struct TargetCard: View {
    let rows: [TargetRow]
    let label: (Int) -> String
    let palette: ChartPalette

    var body: some View {
        DetailCard {
            VStack(alignment: .leading, spacing: 0) {
                CardTitle(title: L("target", "Target")).padding(.bottom, 6)
                ForEach(Array(rows.enumerated()), id: \.offset) { index, row in
                    if index > 0 { Divider().overlay(palette.line) }
                    HStack {
                        Text(label(Int(row.interval))).font(.system(size: 16))
                        Spacer()
                        Text(row.remaining).font(.system(size: 17, weight: .semibold))
                    }
                    .frame(height: 44)
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel("\(label(Int(row.interval))), \(row.remaining)")
                }
            }
        }
    }
}

/// The notes of the habit, when it has any.
struct NotesCard: View {
    let text: String

    var body: some View {
        DetailCard {
            VStack(alignment: .leading, spacing: 8) {
                CardTitle(title: L("notes", "Notes"))
                Text(text).font(.system(size: 15)).foregroundStyle(.secondary)
            }
        }
    }
}
