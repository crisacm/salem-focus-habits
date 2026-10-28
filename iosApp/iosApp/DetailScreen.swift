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

struct DetailScreen: View {
    @Bindable var model: SessionModel
    let habitId: Int64
    /// The months that the score and the history show: 3, 6 or 12.
    @State private var months = 3
    @State private var showHistory = false
    @State private var showDeleteConfirm = false

    private let calendarWeeks = 18

    var body: some View {
        let detail = model.detail(id: habitId)
        ScrollView {
            if let detail, let charts = model.detailCharts(id: habitId) {
                content(detail, charts)
            }
        }
        .background(Color(.systemGroupedBackground).ignoresSafeArea())
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if let detail {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(L("edit", "Edit")) { model.editor = model.editorSnapshot(id: habitId) }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Button(L("edit_history", "Edit history")) { showHistory = true }
                        Button(detail.archived ? L("unarchive", "Unarchive") : L("archive", "Archive")) {
                            if detail.archived {
                                model.unarchive(ids: [KotlinLong(value: habitId)])
                            } else {
                                model.archive(ids: [KotlinLong(value: habitId)])
                            }
                        }
                        Button(L("delete", "Delete"), role: .destructive) { showDeleteConfirm = true }
                    } label: {
                        Image(systemName: "ellipsis")
                    }
                    .accessibilityLabel(L("more", "More"))
                }
            }
        }
        .sheet(isPresented: $showHistory) {
            HistoryEditor(model: model, habitId: habitId)
        }
        .sheet(isPresented: $showDeleteConfirm) {
            DeleteHabitsDialog(count: 1) {
                model.delete(ids: [KotlinLong(value: habitId)])
                model.detailId = nil
            }
        }
    }

    private func content(_ detail: DetailSnapshot, _ charts: DetailCharts) -> some View {
        let tone = habitTone(palette: Int(detail.palette), colorArgb: detail.colorArgb)
        let palette = ChartPalette(accent: tone.accent)
        let today = charts.today
        let start = windowStart(today: today)
        let range = rangeText(start: start, end: today)
        let weekly = months <= 6
        let scores = (weekly ? charts.scoresByWeek : charts.scoresByMonth).compactMap { $0 as? ScorePoint }
        let bars = (weekly ? charts.barsByWeek : charts.barsByMonth).compactMap { $0 as? BarPoint }
        let firstWeekday = Int(model.settings?.firstWeekday ?? 0)
        return VStack(spacing: 15) {
            DetailHeader(detail: detail, tone: tone)
                .padding(.top, 4)
                .padding(.bottom, 8)
            OverviewCard(detail: detail, tone: tone)
            if detail.numerical {
                TargetCard(
                    rows: charts.targets.compactMap { $0 as? TargetRow },
                    label: intervalLabel,
                    palette: palette
                )
            }
            Picker("", selection: $months) {
                Text(L("range_3m", "3M")).tag(3)
                Text(L("range_6m", "6M")).tag(6)
                Text(L("range_1y", "1Y")).tag(12)
            }
            .pickerStyle(.segmented)
            .labelsHidden()
            .controlSize(.large)
            DetailCard {
                VStack(alignment: .leading, spacing: 12) {
                    CardTitle(
                        title: L("score", "Score"),
                        subtitle: range,
                        value: "\(detail.scorePercent)%",
                        caption: L("now", "Now")
                    )
                    ScoreLineChart(
                        points: scores,
                        now: Double(detail.scorePercent) / 100,
                        start: start,
                        end: today,
                        palette: palette
                    )
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel("\(L("score", "Score")) \(detail.scorePercent)%, \(range)")
                }
            }
            DetailCard {
                VStack(alignment: .leading, spacing: 12) {
                    CardTitle(
                        title: L("history", "History"),
                        subtitle: "\(historyKind(detail, weekly: weekly)) \u{00B7} \(range)",
                        value: bars.last.map { amount($0.value, detail) },
                        caption: weekly ? L("this_week", "This week") : L("this_month", "This month")
                    )
                    HistoryBarsChart(bars: bars, start: start, end: today, palette: palette)
                        .accessibilityElement(children: .ignore)
                        .accessibilityLabel("\(L("history", "History")), \(range)")
                }
            }
            DetailCard {
                VStack(alignment: .leading, spacing: 8) {
                    CardTitle(title: L("frequency", "Frequency"), subtitle: L("completions_by_weekday", "Completions by weekday"))
                    WeekdayBarsChart(
                        totals: weekdayTotals(charts, firstWeekday: firstWeekday),
                        names: weekdayNames(firstWeekday: firstWeekday),
                        palette: palette
                    )
                }
            }
            DetailCard {
                VStack(alignment: .leading, spacing: 4) {
                    CardTitle(title: L("best_streaks", "Best streaks"))
                    let streaks = charts.streaks.compactMap { $0 as? StreakItem }
                    if streaks.isEmpty {
                        Text(L("no_streaks", "No streaks yet"))
                            .font(.system(size: 15))
                            .foregroundStyle(.secondary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 24)
                    } else {
                        StreakRowsChart(streaks: streaks, palette: palette, dayText: dayCount)
                    }
                }
            }
            DetailCard {
                VStack(alignment: .leading, spacing: 12) {
                    CardTitle(
                        title: L("calendar", "Calendar"),
                        subtitle: calendarRange(today: today, firstWeekday: firstWeekday)
                    )
                    CalendarDotsChart(
                        days: charts.calendar.compactMap { $0 as? CalendarDay },
                        today: today,
                        firstWeekday: firstWeekday,
                        weeks: calendarWeeks,
                        palette: palette
                    ) { _ in showHistory = true }
                    HStack(spacing: 18) {
                        legend(L("done", "Done"), color: palette.accent)
                        legend(L("missed", "Missed"), color: palette.line)
                    }
                }
            }
            if !detail.notes.isEmpty {
                NotesCard(text: detail.notes)
            }
        }
        .padding(.horizontal, 20)
        .padding(.bottom, 24)
    }

    private func legend(_ text: String, color: Color) -> some View {
        HStack(spacing: 6) {
            RoundedRectangle(cornerRadius: 3, style: .continuous).fill(color).frame(width: 12, height: 12)
            Text(text).font(.system(size: 12)).foregroundStyle(.secondary)
        }
    }

    // MARK: - Texts and ranges

    /// The first day of the oldest month of the range.
    private func windowStart(today: Int32) -> Int32 {
        let calendar = Calendar(identifier: .gregorian)
        var parts = calendar.dateComponents([.year, .month], from: date(fromDaysSince2000: Int(today)))
        parts.day = 1
        guard let first = calendar.date(from: parts),
              let start = calendar.date(byAdding: .month, value: -(months - 1), to: first) else { return today - 90 }
        return dayNumber(of: start)
    }

    /// "Aug – Oct 2026", or with both years when the range crosses one.
    private func rangeText(start: Int32, end: Int32) -> String {
        let calendar = Calendar(identifier: .gregorian)
        let first = date(fromDaysSince2000: Int(start))
        let last = date(fromDaysSince2000: Int(end))
        let month = DateFormatter()
        month.setLocalizedDateFormatFromTemplate("MMM")
        let withYear = DateFormatter()
        withYear.setLocalizedDateFormatFromTemplate("MMMy")
        if calendar.component(.year, from: first) == calendar.component(.year, from: last) {
            return "\(month.string(from: first)) – \(withYear.string(from: last))"
        }
        return "\(withYear.string(from: first)) – \(withYear.string(from: last))"
    }

    /// "Jun 28 – Oct 31, 2026": the first day of the oldest week shown and today.
    private func calendarRange(today: Int32, firstWeekday: Int) -> String {
        let sunday = (Int(today) + 6) % 7
        let sinceWeekStart = (sunday - firstWeekday + 7) % 7
        let first = today - Int32(sinceWeekStart) - Int32((calendarWeeks - 1) * 7)
        let short = DateFormatter()
        short.setLocalizedDateFormatFromTemplate("MMMd")
        let full = DateFormatter()
        full.setLocalizedDateFormatFromTemplate("yMMMd")
        return "\(short.string(from: date(fromDaysSince2000: Int(first)))) – \(full.string(from: date(fromDaysSince2000: Int(today))))"
    }

    private func historyKind(_ detail: DetailSnapshot, weekly: Bool) -> String {
        if detail.numerical {
            return weekly ? L("total_per_week", "Total per week") : L("total_per_month", "Total per month")
        }
        return weekly ? L("days_done_per_week", "Days done per week") : L("days_done_per_month", "Days done per month")
    }

    /// The amount of a bar: days for a yes or no habit, the number and the unit for a measurable one.
    private func amount(_ value: Double, _ detail: DetailSnapshot) -> String {
        let text = value == value.rounded() ? String(Int(value)) : String(format: "%.1f", value)
        if detail.numerical { return detail.unit.isEmpty ? text : "\(text) \(detail.unit)" }
        return "\(text) \(Int(value) == 1 ? L("day_one", "day") : L("day_other", "days"))"
    }

    private func dayCount(_ days: Int) -> String {
        "\(days) \(days == 1 ? L("day_one", "day") : L("day_other", "days"))"
    }

    /// The totals of the days of the week, starting on the first day of the week of the settings.
    private func weekdayTotals(_ charts: DetailCharts, firstWeekday: Int) -> [WeekdayTotal] {
        let all = charts.weekdays.compactMap { $0 as? WeekdayTotal }
        return (0..<7).compactMap { offset in
            let day = (firstWeekday + offset) % 7
            return all.first { Int($0.day) == day }
        }
    }

    private func weekdayNames(firstWeekday: Int) -> [String] {
        let names = Calendar.current.shortWeekdaySymbols
        return (0..<7).map { offset in
            let index = (firstWeekday + offset) % 7
            return names.indices.contains(index) ? names[index] : ""
        }
    }

    private func intervalLabel(_ days: Int) -> String {
        switch days {
        case 1: return L("today", "Today")
        case 7: return L("week", "Week")
        case 30: return L("month", "Month")
        case 91: return L("quarter", "Quarter")
        default: return L("year", "Year")
        }
    }
}

struct HistoryEditor: View {
    @Bindable var model: SessionModel
    let habitId: Int64
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                ForEach(days, id: \.day) { day in
                    HStack {
                        Text(date(fromDaysSince2000: Int(day.day)), format: .dateTime.month().day())
                        Spacer()
                        if day.number > 0 { Text(String(day.number)) }
                        else if day.value == Int32(EntryValue.yesManual) || day.value == Int32(EntryValue.yesAuto) {
                            Image(systemName: "checkmark")
                        }
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        let next = day.value == Int32(EntryValue.yesManual) ? Int32(EntryValue.no) : Int32(EntryValue.yesManual)
                        model.setEntry(habitId, day: day.day, value: next, notes: day.notes)
                        model.reload()
                    }
                }
            }
            .navigationTitle(L("history", "History"))
            .toolbar { Button(L("done", "Done")) { dismiss() } }
        }
    }

    private var days: [HistoryDay] {
        model.history(id: habitId, days: 60)
    }
}
