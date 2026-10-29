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
    @State private var showCalendar = false
    /// The day the rows show; nil is today.
    @State private var selectedDay: Int32?

    @State private var hintText: String?

    var body: some View {
        let snapshot = model.list
        ZStack(alignment: .bottom) {
            Group {
                if let snapshot, !snapshot.rows.isEmpty {
                    habitList(snapshot)
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
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Color(.systemGroupedBackground).ignoresSafeArea())
            .overlay(alignment: .top) { actionBar(snapshot) }
            if let hintText {
                ListHintBanner(text: hintText) { self.hintText = nil }
            }
        }
        .toolbar(.hidden, for: .navigationBar)
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
                    title: target.row.name,
                    day: target.cell.day,
                    unit: target.row.unit,
                    notes: target.cell.notes,
                    number: target.cell.number,
                    skipEnabled: skip,
                    unknownEnabled: unknown
                ) { value, notes in
                    model.setEntry(target.row.id, day: target.cell.day, value: value, notes: notes)
                }
            } else {
                CheckmarkPopup(
                    title: target.row.name,
                    day: target.cell.day,
                    notes: target.cell.notes,
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
        .sheet(isPresented: $showDeleteConfirm, onDismiss: { if !selecting { selected.removeAll() } }) {
            DeleteHabitsDialog(count: selected.count) {
                model.delete(ids: ids)
                selecting = false
                selected.removeAll()
            }
        }
        .sheet(isPresented: $showCalendar) {
            if let snapshot = model.list {
                DayPickerSheet(
                    earliest: date(fromDaysSince2000: Int(days(snapshot).min() ?? snapshot.today)),
                    latest: date(fromDaysSince2000: Int(snapshot.today)),
                    selected: date(fromDaysSince2000: Int(effectiveDay(snapshot)))
                ) { picked in
                    selectDay(dayNumber(of: picked), snapshot: snapshot)
                }
            }
        }
    }

    private var empty: some View {
        VStack(spacing: 12) {
            Text(L("no_habits", "You have no habits yet"))
                .font(.title3)
            Button(L("add_habit", "Add habit")) { showHabitType = true }
        }
    }

    // MARK: - List

    private func habitList(_ snapshot: ListSnapshot) -> some View {
        let day = effectiveDay(snapshot)
        let habits = rows(snapshot)
        return List {
            Section {
                header(snapshot, day: day, habits: habits)
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.clear)
                    .listRowSeparator(.hidden)
            }
            Section {
                ForEach(habits, id: \.id) { row in
                    habitRow(row, day: day)
                        .listRowInsets(EdgeInsets(top: 0, leading: 17.5, bottom: 0, trailing: 17.5))
                        .alignmentGuide(.listRowSeparatorLeading) { _ in 51.5 }
                        .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                            Button(role: .destructive) {
                                selected = [row.id]
                                showDeleteConfirm = true
                            } label: {
                                Label(L("delete", "Delete"), systemImage: "trash")
                            }
                            Button {
                                if row.archived {
                                    model.unarchive(ids: [KotlinLong(value: row.id)])
                                } else {
                                    model.archive(ids: [KotlinLong(value: row.id)])
                                }
                            } label: {
                                Label(
                                    row.archived ? L("unarchive", "Unarchive") : L("archive", "Archive"),
                                    systemImage: "archivebox"
                                )
                            }
                            .tint(.orange)
                        }
                }
            }
        }
        .listStyle(.insetGrouped)
        .listSectionSpacing(0)
    }

    /// The title, the date and the five days that choose which day the rows show.
    private func header(_ snapshot: ListSnapshot, day: Int32, habits: [HabitRow]) -> some View {
        let done = habits.filter { isDone($0, entry(of: $0, day: day)) }.count
        let isToday = day == snapshot.today
        return VStack(alignment: .leading, spacing: 0) {
            Text(isToday ? L("today", "Today") : formatted(day, template: "EEEE"))
                .font(.largeTitle.bold())
                .padding(.horizontal, 20)
            Text("\(formatted(day, template: "EEEEMMMMd")) \u{00B7} \(String(format: L("habits_done_of_total", "%d of %d"), done, habits.count))")
                .font(.system(size: 15))
                .foregroundStyle(.secondary)
                .padding(.horizontal, 20)
                .padding(.top, 4)
            dayStrip(snapshot, selected: day)
                .padding(.top, 17.5)
                .padding(.horizontal, 10)
        }
        .padding(.top, 54)
        .padding(.bottom, 22)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func dayStrip(_ snapshot: ListSnapshot, selected: Int32) -> some View {
        let earliest = days(snapshot).min() ?? snapshot.today
        let end = min(snapshot.today, selected + 2)
        let start = max(earliest, end - 4)
        return HStack(spacing: 0) {
            ForEach(Array(start...max(start, min(snapshot.today, start + 4))), id: \.self) { day in
                let isSelected = day == selected
                Button { selectDay(day, snapshot: snapshot) } label: {
                    VStack(spacing: 4.5) {
                        Text(formatted(day, template: "EEE"))
                            .font(.system(size: 13))
                            .foregroundStyle(.secondary)
                            .frame(height: 16)
                        ZStack {
                            if isSelected { Circle().fill(Color.primary) }
                            Text(formatted(day, template: "d"))
                                .font(.system(size: 22, weight: isSelected ? .semibold : .regular))
                                .foregroundStyle(isSelected ? Color(.systemBackground) : Color.primary)
                        }
                        .frame(width: 40, height: 40)
                    }
                    .frame(maxWidth: .infinity)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(formatted(day, template: "EEEEMMMMd"))
                .accessibilityAddTraits(isSelected ? .isSelected : [])
            }
        }
    }

    private func habitRow(_ row: HabitRow, day: Int32) -> some View {
        let cell = entry(of: row, day: day)
        let tone = habitTone(palette: Int(row.palette), colorArgb: row.colorArgb)
        return HStack(spacing: 0) {
            if selecting {
                Image(systemName: selected.contains(row.id) ? "checkmark.circle.fill" : "circle")
                    .font(.system(size: 22))
                    .foregroundStyle(selected.contains(row.id) ? tone.accent : Color(.systemGray))
                    .padding(.trailing, 12)
            }
            ScoreRing(score: Int(row.score), symbol: habitSymbol(id: row.id), tone: tone)
            Text(row.name)
                .font(.system(size: 17))
                .lineLimit(2)
                .padding(.leading, 14)
            Spacer(minLength: 8)
            if let cell {
                entryControl(row, cell: cell, tone: tone)
                    .padding(.trailing, row.numerical ? 0 : 8)
                    .padding(.leading, 8)
                    .padding(.vertical, 5)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        guard !selecting else { return }
                        if model.list?.shortToggle != true {
                            entryPopup = EntryPopupTarget(row: row, cell: cell, numerical: row.numerical)
                        } else {
                            model.toggle(row.id, day: cell.day)
                        }
                    }
                    .onLongPressGesture {
                        guard !selecting else { return }
                        entryPopup = EntryPopupTarget(row: row, cell: cell, numerical: row.numerical)
                    }
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel(accessibilityLabel(row, cell: cell))
                    .accessibilityAddTraits(.isButton)
            }
            Image(systemName: "chevron.right")
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(Color(.tertiaryLabel))
                .padding(.leading, 8)
                .accessibilityHidden(true)
        }
        .frame(minHeight: 56)
        .contentShape(Rectangle())
        .onTapGesture {
            if selecting {
                if selected.contains(row.id) { selected.remove(row.id) } else { selected.insert(row.id) }
            } else {
                model.detailId = row.id
            }
        }
    }

    @ViewBuilder
    private func entryControl(_ row: HabitRow, cell: DayCell, tone: HabitTone) -> some View {
        if row.numerical {
            ValuePill(text: amountText(row, cell: cell), tone: tone, reached: isDone(row, cell))
        } else if isDone(row, cell) {
            HabitCheck(tone: tone, done: true)
        } else if cell.value == Int32(EntryValue.skip) {
            HabitCheck(tone: tone, done: false, mark: "\u{2013}")
        } else if cell.value == Int32(EntryValue.unknown) {
            HabitCheck(tone: tone, done: false, mark: "?")
        } else {
            HabitCheck(tone: tone, done: false)
        }
    }

    // MARK: - Bar

    @ViewBuilder
    private func actionBar(_ snapshot: ListSnapshot?) -> some View {
        HStack(spacing: 10) {
            Spacer(minLength: 0)
            if selecting {
                selectionBar
            } else {
                HStack(spacing: 0) {
                    Button { showCalendar = true } label: { BarIcon(systemName: "calendar") }
                        .accessibilityLabel(L("calendar", "Calendar"))
                        .disabled(snapshot == nil)
                    Menu {
                        Button(L("sort", "Sort")) { model.cyclePrimaryOrder() }
                        Toggle(L("show_archived", "Show archived"), isOn: Binding(
                            get: { snapshot?.showArchived == true },
                            set: { model.setShowArchived(show: $0) }
                        ))
                        Toggle(L("show_completed", "Show completed"), isOn: Binding(
                            get: { snapshot?.showCompleted != false },
                            set: { model.setShowCompleted(show: $0) }
                        ))
                    } label: { BarIcon(systemName: "line.3.horizontal.decrease") }
                        .accessibilityLabel(L("filter", "Filter"))
                    Menu {
                        Button(L("select", "Select")) { selecting = true }
                            .disabled(snapshot?.rows.isEmpty != false)
                        Button(L("settings", "Settings")) { model.showSettings = true }
                        Button(L("about", "About")) { model.showAbout = true }
                    } label: { BarIcon(systemName: "ellipsis") }
                        .accessibilityLabel(L("more", "More"))
                }
                .floatingGlass(in: Capsule())
                AddHabitButton { showHabitType = true }
            }
        }
        .padding(.horizontal, 20)
    }

    private var selectionBar: some View {
        HStack(spacing: 8) {
            Button(L("archive", "Archive")) {
                model.archive(ids: ids)
                selecting = false
                selected.removeAll()
            }
            .disabled(selected.isEmpty)
            .padding(.horizontal, 12)
            Button(L("delete", "Delete")) { showDeleteConfirm = true }
                .disabled(selected.isEmpty)
                .padding(.horizontal, 12)
            Button(L("done", "Done")) { selecting = false; selected.removeAll() }
                .fontWeight(.semibold)
                .padding(.horizontal, 12)
        }
        .foregroundStyle(Color.primary)
        .padding(.horizontal, 8)
        .frame(height: 44)
        .floatingGlass(in: Capsule())
    }

    // MARK: - Data

    private var ids: [KotlinLong] {
        selected.map { KotlinLong(value: $0) }
    }

    private func rows(_ snapshot: ListSnapshot) -> [HabitRow] {
        snapshot.rows.compactMap { $0 as? HabitRow }
    }

    private func days(_ snapshot: ListSnapshot) -> [Int32] {
        snapshot.days.compactMap { ($0 as? KotlinInt)?.int32Value ?? ($0 as? Int32) }
    }

    private func entry(of row: HabitRow, day: Int32) -> DayCell? {
        row.cells.compactMap { $0 as? DayCell }.first { $0.day == day }
    }

    /// The day the rows show: the one the person chose while it is still loaded, today otherwise.
    private func effectiveDay(_ snapshot: ListSnapshot) -> Int32 {
        guard let selectedDay, selectedDay <= snapshot.today, days(snapshot).contains(selectedDay) else {
            return snapshot.today
        }
        return selectedDay
    }

    private func selectDay(_ day: Int32, snapshot: ListSnapshot) {
        selectedDay = day == snapshot.today ? nil : day
    }

    /// Whether a day counts as done: a yes for a yes or no habit, the target reached for a numerical one.
    private func isDone(_ row: HabitRow, _ cell: DayCell?) -> Bool {
        guard let cell else { return false }
        if row.numerical {
            return row.target > 0 ? cell.number >= row.target : cell.number > 0
        }
        return cell.value == Int32(EntryValue.yesManual) || cell.value == Int32(EntryValue.yesAuto)
    }

    private func amountText(_ row: HabitRow, cell: DayCell) -> String {
        let number = cell.number
        let text = number == number.rounded() ? String(Int(number)) : String(format: "%.1f", number)
        return row.unit.isEmpty ? text : "\(text) \(row.unit)"
    }

    private func accessibilityLabel(_ row: HabitRow, cell: DayCell) -> String {
        if row.numerical { return "\(row.name), \(amountText(row, cell: cell))" }
        return "\(row.name), \(isDone(row, cell) ? L("yes", "Yes") : L("no", "No"))"
    }

    private func formatted(_ day: Int32, template: String) -> String {
        let formatter = appDateFormatter()
        formatter.setLocalizedDateFormatFromTemplate(template)
        return formatter.string(from: date(fromDaysSince2000: Int(day)))
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

/// The number of days since 2000-01-01 of a date, as the habit snapshots count them.
func dayNumber(of day: Date) -> Int32 {
    let calendar = Calendar(identifier: .gregorian)
    let base = calendar.startOfDay(for: date(fromDaysSince2000: 0))
    return Int32(calendar.dateComponents([.day], from: base, to: calendar.startOfDay(for: day)).day ?? 0)
}

/// Lets the person jump to any day that the list has loaded.
private struct DayPickerSheet: View {
    let earliest: Date
    let latest: Date
    @State var selected: Date
    let onPicked: (Date) -> Void
    @Environment(\.dismiss) private var dismiss

    init(earliest: Date, latest: Date, selected: Date, onPicked: @escaping (Date) -> Void) {
        self.earliest = earliest
        self.latest = latest
        _selected = State(initialValue: selected)
        self.onPicked = onPicked
    }

    var body: some View {
        NavigationStack {
            DatePicker("", selection: $selected, in: earliest...latest, displayedComponents: .date)
                .datePickerStyle(.graphical)
                .labelsHidden()
                .padding()
                .onChange(of: selected) { _, picked in
                    onPicked(picked)
                    dismiss()
                }
            Spacer()
        }
        .presentationDetents([.medium])
    }
}
