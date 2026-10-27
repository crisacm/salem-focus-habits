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
    @State private var offsets: [Int32: Int32] = [:]
    @State private var showHistory = false

    var body: some View {
        let detail = model.detail(id: habitId)
        ScrollView {
            if let detail {
                VStack(alignment: .leading, spacing: 16) {
                    Text(detail.question).font(.subheadline).foregroundStyle(.secondary)
                    Text(detail.frequency)
                    if !detail.reminder.isEmpty { Text(detail.reminder) }
                    stats(detail)
                    chart(detail, card: 0, title: L("calendar", "Calendar"), height: 160)
                    Button(L("edit", "Edit")) { showHistory = true }
                    chart(detail, card: 5, title: L("history", "History"), height: 200, spinner: true)
                    chart(detail, card: 1, title: L("score", "Score"), height: 200, spinner: true)
                    chart(detail, card: 2, title: L("frequency", "Frequency"), height: 220)
                    chart(detail, card: 3, title: L("streaks", "Streaks"), height: measured(3, fallback: 120))
                    chart(detail, card: 4, title: L("target", "Target"), height: measured(4, fallback: 160))
                    if !detail.notes.isEmpty { Text(detail.notes) }
                }
                .padding()
            }
        }
        .navigationTitle(detail?.name ?? "")
        .toolbar {
            if let detail {
                Button(L("edit", "Edit")) { model.editor = model.editorSnapshot(id: habitId) }
                Button(detail.archived ? L("unarchive", "Unarchive") : L("archive", "Archive")) {
                    if detail.archived {
                        model.unarchive(ids: [KotlinLong(value: habitId)])
                    } else {
                        model.archive(ids: [KotlinLong(value: habitId)])
                    }
                }
            }
        }
        .sheet(isPresented: $showHistory) {
            HistoryEditor(model: model, habitId: habitId)
        }
    }

    private func stats(_ detail: DetailSnapshot) -> some View {
        HStack {
            stat(L("score", "Score"), "\(detail.scorePercent)%")
            stat(L("month", "Month"), detail.scoreMonth)
            stat(L("year", "Year"), detail.scoreYear)
            stat(L("total", "Total"), "\(detail.total)")
            stat(L("streaks", "Streaks"), "\(detail.bestStreak)")
        }
    }

    private func stat(_ title: String, _ value: String) -> some View {
        VStack {
            Text(value).font(.headline)
            Text(title).font(.caption2).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
    }

    private func chart(_ detail: DetailSnapshot, card: Int32, title: String, height: Double, spinner: Bool = false) -> some View {
        VStack(alignment: .leading) {
            HStack {
                Text(title).font(.headline).foregroundStyle(habitColor(detail.colorArgb))
                Spacer()
                if spinner {
                    Button(intervalName(card: card, detail: detail)) { spin(card, detail) }
                        .font(.caption)
                }
            }
            ChartCanvas(
                ops: ops(card, width: 360, height: height, detail: detail),
                onTap: { point in
                    guard card == 0 else { return }
                    let day = model.chartTap(
                        id: habitId,
                        card: card,
                        x: point.x,
                        y: point.y,
                        width: 360,
                        height: height,
                        offset: offsets[card] ?? 0
                    )
                    if day >= 0 { showHistory = true }
                }
            )
            .frame(height: height)
            .gesture(DragGesture().onChanged { value in
                let delta = Int32(-value.translation.width / 12)
                offsets[card] = max(0, delta)
            })
        }
    }

    private func ops(_ card: Int32, width: Double, height: Double, detail: DetailSnapshot) -> [DrawOp] {
        let labels = (0..<detail.targetIntervals.count).map { index in
            intervalLabel(Int(detail.targetIntervals[index].int32Value))
        }
        return model.chartOps(
            id: habitId,
            card: card,
            width: width,
            height: height,
            offset: offsets[card] ?? 0,
            labels: labels
        )
    }

    private func measured(_ card: Int32, fallback: Double) -> Double {
        let height = model.chartHeight(id: habitId, card: card)
        return height > 1 ? height : fallback
    }

    private func spin(_ card: Int32, _ detail: DetailSnapshot) {
        let next = (card == 1 ? detail.scoreSpinner : detail.barSpinner) + 1
        if card == 1 {
            model.setScoreSpinner(position: next % 5)
        } else {
            model.setBarSpinner(numerical: detail.numerical, position: next % (detail.numerical ? 5 : 4))
        }
    }

    private func intervalName(card: Int32, detail: DetailSnapshot) -> String {
        let names = detail.numerical || card == 1
            ? [L("day", "Day"), L("week", "Week"), L("month", "Month"), L("quarter", "Quarter"), L("year", "Year")]
            : [L("week", "Week"), L("month", "Month"), L("quarter", "Quarter"), L("year", "Year")]
        let index = Int(card == 1 ? detail.scoreSpinner : detail.barSpinner)
        return names[min(index, names.count - 1)]
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
