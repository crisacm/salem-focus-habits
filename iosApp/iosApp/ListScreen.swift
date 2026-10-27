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

struct ListScreen: View {
    @Bindable var model: SessionModel
    @State private var selecting = false
    @State private var selected: Set<Int64> = []
    @State private var entryPopup: EntryPopupTarget?
    @State private var showHabitType = false
    @State private var showDeleteConfirm = false

    @State private var hintText: String?

    var body: some View {
        let snapshot = model.list
        ZStack(alignment: .bottom) {
            VStack(spacing: 0) {
                if let snapshot, !snapshot.rows.isEmpty {
                    dayHeader(snapshot)
                    List(selection: selecting ? $selected : nil) {
                        ForEach(rows(snapshot), id: \.id) { row in
                            habitRow(row, days: days(snapshot))
                                .tag(row.id)
                        }
                    }
                    .listStyle(.plain)
                    .environment(\.editMode, .constant(selecting ? .active : .inactive))
                } else if let error = model.loadError {
                    Text(error)
                        .foregroundStyle(.red)
                        .multilineTextAlignment(.center)
                        .padding()
                } else if snapshot != nil {
                    empty
                } else {
                    ProgressView()
                }
            }
            if let hintText {
                ListHintBanner(text: hintText) { self.hintText = nil }
            }
        }
        .navigationTitle(L("main_activity_title", "Habits"))
        .toolbar { toolbar(snapshot) }
        .onChange(of: model.list?.today) { _, today in
            if let today { loadHintIfNeeded(today: today) }
        }
        .onAppear {
            if let today = model.list?.today { loadHintIfNeeded(today: today) }
        }
        .sheet(item: $entryPopup) { target in
            let skip = model.list?.skipEnabled == true
            let unknown = model.list?.questionMarks == true
            if target.numerical {
                NumberPopup(
                    notes: target.cell.notes,
                    number: target.cell.number,
                    skipEnabled: skip,
                    unknownEnabled: unknown
                ) { value, notes in
                    model.setEntry(target.row.id, day: target.cell.day, value: value, notes: notes)
                }
            } else {
                CheckmarkPopup(
                    habitColor: habitColor(target.row.colorArgb),
                    notes: target.cell.notes,
                    value: target.cell.value,
                    skipEnabled: skip,
                    unknownEnabled: unknown
                ) { value, notes in
                    model.setEntry(target.row.id, day: target.cell.day, value: value, notes: notes)
                }
            }
        }
        .sheet(isPresented: $showHabitType) {
            HabitTypeDialog { type in
                model.editor = model.blankEditor(type: type)
            }
        }
        .sheet(isPresented: $showDeleteConfirm) {
            DeleteHabitsDialog(count: selected.count) {
                model.delete(ids: ids)
                selecting = false
                selected.removeAll()
            }
        }
    }

    private var empty: some View {
        VStack(spacing: 12) {
            Text(L("no_habits", "You have no habits yet"))
                .font(.title3)
            Button(L("add_habit", "Add habit")) { showHabitType = true }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private func dayHeader(_ snapshot: ListSnapshot) -> some View {
        HStack {
            Spacer()
            ForEach(days(snapshot), id: \.self) { day in
                Text(weekday(day))
                    .font(.caption2)
                    .frame(width: 36)
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 6)
    }

    private func habitRow(_ row: HabitRow, days: [Int32]) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(row.name).font(.body)
                Text("\(row.score)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer(minLength: 8)
            ForEach(cells(row), id: \.day) { cell in
                CheckCell(color: habitColor(row.colorArgb), value: cell.value, numerical: row.numerical, number: cell.number)
                    .onTapGesture {
                        guard !selecting else { return }
                        if row.numerical && model.list?.shortToggle != true {
                            entryPopup = EntryPopupTarget(row: row, cell: cell, numerical: true)
                        } else {
                            model.toggle(row.id, day: cell.day)
                        }
                    }
                    .onLongPressGesture {
                        guard !selecting else { return }
                        entryPopup = EntryPopupTarget(row: row, cell: cell, numerical: row.numerical)
                    }
            }
        }
        .contentShape(Rectangle())
        .onTapGesture { if !selecting { model.detailId = row.id } }
    }

    @ToolbarContentBuilder
    private func toolbar(_ snapshot: ListSnapshot?) -> some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Menu {
                Button(L("settings", "Settings")) { model.showSettings = true }
                Button(L("about", "About")) { model.showAbout = true }
                Button(L("sort", "Sort")) { model.cyclePrimaryOrder() }
                Toggle(L("show_archived", "Show archived"), isOn: Binding(
                    get: { snapshot?.showArchived == true },
                    set: { model.setShowArchived(show: $0) }
                ))
                Toggle(L("show_completed", "Show completed"), isOn: Binding(
                    get: { snapshot?.showCompleted != false },
                    set: { model.setShowCompleted(show: $0) }
                ))
            } label: { Image(systemName: "line.3.horizontal") }
        }
        ToolbarItemGroup(placement: .topBarTrailing) {
            if selecting {
                Button(L("archive", "Archive")) {
                    model.archive(ids: ids)
                    selecting = false
                    selected.removeAll()
                }
                Button(L("delete", "Delete")) { showDeleteConfirm = true }
                Button(L("done", "Done")) { selecting = false; selected.removeAll() }
            } else {
                Button { selecting = true } label: { Image(systemName: "checkmark.circle") }
                Button { showHabitType = true } label: { Image(systemName: "plus") }
            }
        }
    }

    private var ids: [KotlinLong] {
        selected.map { KotlinLong(value: $0) }
    }

    private func rows(_ snapshot: ListSnapshot) -> [HabitRow] {
        snapshot.rows.compactMap { $0 as? HabitRow }
    }

    private func days(_ snapshot: ListSnapshot) -> [Int32] {
        snapshot.days.compactMap { ($0 as? KotlinInt)?.int32Value ?? ($0 as? Int32) }
    }

    private func cells(_ row: HabitRow) -> [DayCell] {
        row.cells.compactMap { $0 as? DayCell }
    }

    private func weekday(_ day: Int32) -> String {
        let symbol = DateFormatter()
        symbol.dateFormat = "EEEEE"
        return symbol.string(from: date(fromDaysSince2000: Int(day)))
    }

    private func loadHintIfNeeded(today: Int32) {
        guard hintText == nil, shouldShowHint(today: today) else { return }
        let next = UserDefaults.standard.integer(forKey: "last_hint_number") + 1
        let hints = [
            L("hint_drag", "To rearrange the entries, press-and-hold on the name of the habit, then drag it to the correct place."),
            L("hint_landscape", "You can see more days by putting your phone in landscape mode.")
        ]
        guard next < hints.count else { return }
        markHintShown(index: next, today: today)
        hintText = hints[next]
    }

    private func shouldShowHint(today: Int32) -> Bool {
        guard let raw = UserDefaults.standard.object(forKey: "last_hint_timestamp") as? NSNumber else { return false }
        return daysSince2000(unixMs: raw.int64Value) < Int(today)
    }

    private func markHintShown(index: Int, today: Int32) {
        UserDefaults.standard.set(index, forKey: "last_hint_number")
        UserDefaults.standard.set(Int64(946_684_800_000) + Int64(today) * 86_400_000, forKey: "last_hint_timestamp")
    }

    private func daysSince2000(unixMs: Int64) -> Int {
        Int((unixMs - 946_684_800_000) / 86_400_000)
    }
}

private struct ListHintBanner: View {
    let text: String
    let dismiss: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(L("hint_title", "Did you know?"))
                .font(.subheadline.bold())
            Text(text)
                .font(.subheadline)
        }
        .foregroundStyle(.white)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color(red: 0.25, green: 0.32, blue: 0.71))
        .onTapGesture(perform: dismiss)
    }
}

struct CheckCell: View {
    let color: Color
    let value: Int32
    let numerical: Bool
    let number: Double

    var body: some View {
        ZStack {
            if numerical && value > 0 {
                Text(amount)
                    .font(.caption2)
                    .foregroundStyle(color)
            } else {
                Circle()
                    .strokeBorder(color, lineWidth: 2)
                    .background(Circle().fill(filled ? color : .clear))
                if value == Int32(EntryValue.skip) {
                    Text("–").font(.caption2).foregroundStyle(.secondary)
                } else if value == Int32(EntryValue.unknown) {
                    Text("?").font(.caption2).foregroundStyle(.secondary)
                }
            }
        }
        .frame(width: 36, height: 36)
    }

    private var filled: Bool {
        value == Int32(EntryValue.yesManual) || value == Int32(EntryValue.yesAuto)
    }

    private var amount: String {
        number == number.rounded() ? String(Int(number)) : String(format: "%.1f", number)
    }
}

extension HabitRow: @retroactive Identifiable {}
